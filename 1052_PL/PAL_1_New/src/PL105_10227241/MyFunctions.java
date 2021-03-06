package PL105_10227241;

import java.util.Vector;

class MyFunctions {
  
  // -------------------------------------------------------------------------------
  // Function Name : cons
  // 把兩個參數結合成一個dotted pair
  static Node Cons( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    ans.SetLeft( vec.get( 0 ) );
    ans.SetRight( vec.get( 1 ) );
    return ans ;
  } // Cons()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : list
  // 把所有參數結合成一個list
  static Node List( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Node head = new Node() ;
    ans = head ;
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      head.SetLeft( vec.get( i ) );
      Node temp = new Node() ;
      head.SetRight( temp ) ;
      if ( i != vec.size()-1 ) head = head.GetRight() ;  // 如果是最後一個參數就不要在往下一層跑
    } // for
    
    Node nu = null ;           // 風格檢查要求的
    head.SetRight( nu ) ;  // 把最後一層的右節點設為null
    
    // 參數數量為0, 即回傳nil
    if ( vec.size() == 0 ) {
      Token temp = new Token( "nil", 0, 0 ) ;
      temp.Classify() ;
      ans.SetToken( temp ) ;
    } // if
    
    return ans ;
  } // List()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : define
  // 定義參數的意義, parameter1的定義為parameter2
  static void Define( Node node ) throws MyException {
    // _______________________________ 變數宣告區 _______________________________
    
    Node para1 = node.GetLeft() ;              // 參數1, 被定義的symbol
    Node para2 = node.GetRight().GetLeft() ;   // 參數2, Binding (value)
                                                                                
    DefObject def1 = null, def2 = null, new_def = null ;                        
    Boolean para1_IsDefined = false ;      // 參數1有沒有被定義過 (即sVec_Def存不存在參數1)
    
    int para_num = 0 ;                                    // define function 用
    Vector<DefObject> f_para = new Vector<DefObject>() ;  // define function 用
    Vector<Node> f_body = new Vector<Node>() ;            // define function 用
    
    // _______________________________ 變數宣告區 _______________________________
    
    
    // _______________________________ 處理參數1 _______________________________
    
    // 參數1是Symbol
    if ( para1.GetToken() != null && para1.GetToken().GetIntType() == Type.SYMBOL ) {
      // 如果要定義一個symbol的binding, define後面只接受兩個參數(symbol name & binding)
      // 若有兩個以上的參數即丟出error msg
      if ( node.GetRight().GetRight() != null && node.GetRight().GetRight().GetToken() == null
           && node.GetRight().GetRight().GetLeft() != null )
        throw new MyException( "", 87 ) ;

      def1 = Evaluate.FindDefine( para1, true ) ;    // 尋找是否被定義過, 名字或Binding相等
    } // if


    // 參數1是List, 即須符合自定義Function的格式
    // (define ( ... ) ......) // 定義function
    // =================================================================
    else if ( Evaluate.ParameterType( para1 ).equals( "list" ) ) {
      if ( para1.GetLeft().GetToken().GetIntType() != Type.SYMBOL ) 
        throw new MyException( "", 87 ) ;
      
      else {
        // 取得 function name
        String f_name = para1.GetLeft().GetToken().PrintToken() ;
        DefObject def_func = Evaluate.FindDefine( new Node( new Token( f_name, 0, 0 ) ),
                                                  true ) ;
        // 是系統內建function, 故不能被重複定義 -> 丟error msg
        if ( def_func != null && def_func.mType.equals( "function" ) )
          throw new MyException( "", 87 ) ;
        
        // 被定義過
        else if ( def_func != null ) {
          Main.sVec_Def.remove( def_func ) ;   // para1_IsDefined = true ;
        } // else if
        
        // 沒被定義過
        else ;
        
        
        // ------ 取得 ( function_name  { function_para } ) ------ 
        para1 = para1.GetRight() ;
        while ( para1 != null ) {
          if ( para1.GetToken() != null && para1.GetToken().GetIntType() == Type.NIL ) ;
          
          // 右節點是個ATOM NODE
          else if ( para1.GetToken() != null ) throw new MyException( "", 87 ) ;
          
          else if ( para1.GetLeft() != null && para1.GetLeft().GetToken() != null 
                    && para1.GetLeft().GetToken().GetIntType() != Type.SYMBOL )
            throw new MyException( "", 87 ) ;
          
          else {
            para_num ++ ;
            Node nu = null ;
            DefObject p = new DefObject( para1.GetLeft().GetToken().PrintToken(), 
                                         "symbol", nu ) ;
            f_para.add( p ) ;
          } // else
          
          para1 = para1.GetRight() ;
        } // while
        // ------ 取得 ( function_name  { function_para } ) ------
        
        
        // ------------------ 取得 function body ------------------
        Node para_f_body = node.GetRight() ;
        
        while ( para_f_body != null ) {
          if ( ! Evaluate.IsPureList( para_f_body.GetLeft() ) ) throw new MyException( "", 87 ) ;
          
          if ( para_f_body.GetToken() != null && para_f_body.GetToken().GetIntType() == Type.NIL ) ;
          
          // 右節點是個ATOM NODE
          else if ( para_f_body.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
          
          else f_body.add( para_f_body.GetLeft() ) ;
          
          para_f_body = para_f_body.GetRight() ;
        } // while
        // ------------------ 取得 function body ------------------
        
        
        new_def = new DefObject( f_name, "function_lambda",
                                           new Node( new Token( "#<procedure " + f_name  + ">", 0, 0 ) ),
                                           para_num, "s-exp", f_para, f_body ) ;
        
        Main.sVec_Def.add( new_def ) ;
        
        if ( Main.sIsVerbose )     // verbose mode開啟, 才要印一個換行
          System.out.println( f_name + " defined" );
      } // else
      
      return ;
    } // else if 參數1 是 list, 即須符合自定義Function的格式
    // =================================================================

    // define format error
    else throw new MyException( "", 87 ) ;
    
    
    // ------------------- 看參數1有沒有被定義過 -------------------
    // 被定義過且非內建function
    if ( def1 != null && ! def1.GetType().equals( "function" ) ) {
      para1_IsDefined = true ;
      // 先記著有被定義過,還不能remove掉,因為如果參數2有用到原本的定義就找不到原本的定義了
      // Main.sVec_Def.remove( def1 ) ;
    } // if

    // 被定義過且為內建function
    else if ( def1 != null && def1.GetType().equals( "function" ) )
      // 參數1為系統內建Function
      throw new MyException( "", 87 ) ;
    
    // def1 == null
    else ;
    // ------------------- 看參數1有沒有被定義過 -------------------
    
    // _______________________________ 處理參數1 _______________________________
    
    
    // _______________________________ 處理參數2 _______________________________
    
    Node para2_eval = new Node() ;
    try {
      para2_eval = Evaluate.EvalSExp( para2, 1 ) ;
    } catch ( MyException e ) {
      // 要定義的binding evaluate完 No return value -> error
      if ( e.GetCase() == 88 ) 
        throw new MyException( "ERROR (no return value) : " + 
                               BT.PrintLTree( para2, 0, false, "" ), 23 ) ;
      else throw e ;
    } // catch
    
    if ( para2_eval.GetToken() != null ) {
      // System.out.println(para2_eval.GetToken().PrintToken() + para2_eval.GetToken().GetStringType() ) ;
      def2 = Evaluate.FindDefine( para2_eval, false ) ;
      // 為了處理 (define x (quote a)) , 所以FindDefine參數傳false
      // 要binding跟token一樣才算被定義過, 名字跟token一樣 不用去找它的binding
      // 因為 'a 就像 'cons 一樣, 就算 cons 看起來是有定義過, 但事實上不是 binding 到 #<procedure cons>
    } // if
      
    // 這時候再刪掉原本的定義, 因為參數2已被evaluate
    if ( para1_IsDefined ) Main.sVec_Def.remove( def1 ) ;
    
    // 參數2是除了symbol外的atom node (int,float,string,#t,nil)
    // 不需要管def2有沒有找到, 不然 (define a 5 ) (define b a ) 這個case def2為非null
    // 直接binding
    if ( para2_eval.GetToken() != null && Evaluate.IsATOM( para2_eval.GetToken() ) ) {
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    } // if
    
    
    // Define + Lambda
    else if ( para2_eval.GetToken() != null &&
              para2_eval.GetToken().PrintToken().equals( "#<procedure lambda>" ) ) {
      
      // (define x (lambda ( a b ) ( + a 5 ) ( + b 5 )))
      if ( def2 == null ) {
        new_def = Main.sLambda_Temp ;
        new_def.SetName( para1.GetToken().PrintToken() ) ;
      } // if
      
      // (define x (lambda ( a b ) ( + a 5 ) ( + b 5 )))
      // (define y x )
      else {
        new_def = new DefObject( para1.GetToken().PrintToken(), def2.GetType(), def2.GetBinding(), 
                                 def2.GetParameterNumber(), def2.GetParameterType(), 
                                 def2.GetFunctionParameter(), def2.GetFunctionBody() ) ;
      } // else  
    } // else if
    
    // 參數2是經過quote處理過的symbol atom node
    // ex : (define a 'hi)
    else if ( para2_eval.GetToken() != null && def2 == null
              && para2_eval.GetToken().GetIntType() == Type.SYMBOL )
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    
    // 參數2是非ATOM Node 且 沒被定義過  ( 如 : (1 . 2) )
    // 直接binding
    else if ( para2_eval.GetToken() == null && def2 == null )
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    
    // 參數2是被定義過的function
    // 直接binding (defObj的type為function_user)
    else if ( def2 != null && ( def2.GetType().equals( "function" ) || 
                                def2.GetType().equals( "function_user" ) ) ) {
      new_def = new DefObject( para1.GetToken().PrintToken(), "function_user", 
                               def2.GetParameterNumber(), def2.GetParameterType() ) ;
      // System.out.println( def2.GetBinding().GetToken().PrintToken() ) ;
      new_def.SetBinding( def2.GetBinding() ) ;
    } // else if
    
    
    // 參數2是被定義過的function且為使用者自己定義的function
    // 直接binding (defObj的type為function_lambda)
    else if ( def2 != null && def2.GetType().equals( "function_lambda" ) ) {
      new_def = new DefObject( para1.GetToken().PrintToken(),
                               "function_lambda", def2.GetBinding(), 
                               def2.GetParameterNumber(), def2.GetParameterType(),
                               def2.GetFunctionParameter(), def2.GetFunctionBody() ) ;
    } // else if
    
    
    // ERROR Object
    else if ( para2_eval.GetToken() != null && para2_eval.GetToken().GetIntType() == Type.ERROR ) {
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    } // if
    
    
    // ************************
    else throw new MyException( "No Binding", 2222 ) ;
    // ************************
    
    // _______________________________ 處理參數2 _______________________________
    
    
    // _______________________________ 處理回傳值  _______________________________
    
    Main.sVec_Def.add( new_def ) ;
    
    if ( Main.sIsVerbose )      // verbose mode開啟, 才要印一個換行
      System.out.println( para1.GetToken().PrintToken() + " defined" ) ;
    
    return ;
    // _______________________________ 處理回傳值  _______________________________

  } // Define()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : car
  // 取List的第一個Element
  static Node Car( Vector<Node> vec ) throws MyException {
    if ( Evaluate.ParameterType( vec.get( 0 ) ).equals( "list" ) )
      return vec.get( 0 ).GetLeft() ;   // 存進Vector時已經Evaluate過了,故不必再Evaluate
    else throw new MyException( "ERROR (car with incorrect argument type) : "
                                + BT.PrintLTree( vec.get( 0 ), 0, false, "" ), 7 ) ;
  } // Car()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : cdr
  // 取List除了第一個外,剩下的Element
  static Node Cdr( Vector<Node> vec ) throws MyException {
    if ( Evaluate.ParameterType( vec.get( 0 ) ).equals( "list" ) ) {
      
      // 如果右邊節點為null, 則把它改為一個nil的atom node
      if ( vec.get( 0 ).GetRight() == null ) {
        Token t = new Token( "nil", 0, 0 ) ;
        t.Classify() ;
        vec.get( 0 ).SetRight( new Node( t ) ) ;
      } // if
      
      return vec.get( 0 ).GetRight() ;
    } // if
    else throw new MyException( "ERROR (cdr with incorrect argument type) : "
                                + BT.PrintLTree( vec.get( 0 ), 0, false, "" ), 7 ) ;
  } // Cdr()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Clean-Environment
  // 清除使用者定義的Symbol或Function
  static void Clean_Environment() {
    Main.sVec_Def.clear() ;
    Main.sVec_Def = Evaluate.InitDefObject() ;
    if ( Main.sIsVerbose ) System.out.print( "environment cleaned" ) ;
    System.out.println() ;  // 不論verbose mode是否開啟, 都要多印一個換行
  } // Clean_Environment()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : +
  // return所有參數加總後的結果
  static Node Add( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_ans = 0 ;
    double f_ans = 0 ;
    boolean allInteger = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (+ with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      for ( int i = 0 ; i < vec.size() ; i++ ) {
        i_ans = i_ans + Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Integer.toString( i_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      for ( int i = 0 ; i < vec.size() ; i++ ) {
        f_ans = f_ans + Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Double.toString( f_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // else
    
    return ans ;
  } // Add()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : -
  // return所有參數從左到右相減後的結果
  static Node Sub( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_ans = 0 ;
    double f_ans = 0 ;
    boolean allInteger = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (- with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為初始值, 然後減掉之後的參數
      i_ans = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        i_ans = i_ans - Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Integer.toString( i_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為初始值, 然後減掉之後的參數
      f_ans = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        f_ans = f_ans - Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Double.toString( f_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // else
    
    return ans ;
  } // Sub()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : *
  // return所有參數從左到右相乘後的結果
  static Node Mult( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_ans = 0 ;
    double f_ans = 0 ;
    boolean allInteger = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (* with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為初始值, 然後乘以之後的參數
      i_ans = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        i_ans = i_ans * Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Integer.toString( i_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為初始值, 然後乘以之後的參數
      f_ans = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        f_ans = f_ans * Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Double.toString( f_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // else
    
    return ans ;
  } // Mult()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : /
  // return所有參數從左到右相除後的結果, 若除數為0則丟出Error Message
  static Node Div( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_ans = 0 ;
    double f_ans = 0 ;
    boolean allInteger = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (/ with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為初始值, 然後除以之後的參數
      i_ans = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      // 檢查除數(後面的參數)是否為0, 若為0則丟出Error Message
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_par == 0 ) throw new MyException( "ERROR (division by zero) : /\n", 13 ) ;
        else i_ans = i_ans / Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Integer.toString( i_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為初始值, 然後除以之後的參數
      f_ans = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      // 檢查除數(後面的參數)是否為0, 若為0則丟出Error Message
      for ( int i = 1 ; i < vec.size() ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_par == 0 ) throw new MyException( "ERROR (division by zero) : /\n", 13 ) ;
        else f_ans = f_ans / Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
      } // for
      
      Token t = new Token( Double.toString( f_ans ), 0, 0 ) ;
      t.Classify() ;
      ans.SetToken( t ) ;
    } // else
    
    return ans ;
  } // Div()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : >
  // 判斷所有參數是否都大於它後面的參數, 是的話return #t (true), 否則return nil (false)
  static Node IsGreaterThan( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_cmp = 0 ;        // 作為比較的基準值
    double f_cmp = 0 ;     // 作為比較的基準值
    boolean allInteger = true, allTrue = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (> with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      i_cmp = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_cmp > i_par ) i_cmp = i_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      f_cmp = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_cmp > f_par ) f_cmp = f_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // else
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    
    return ans ;
  } // IsGreaterThan()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : >=
  // 判斷所有參數是否都大於或等於它後面的參數, 是的話return #t (true), 否則return nil (false)
  static Node IsGreaterThanOrEqual( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_cmp = 0 ;       // 作為比較的基準值
    double f_cmp = 0 ;     // 作為比較的基準值
    boolean allInteger = true, allTrue = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (>= with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      i_cmp = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_cmp >= i_par ) i_cmp = i_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      f_cmp = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_cmp >= f_par ) f_cmp = f_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // else
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    
    return ans ;
  } // IsGreaterThanOrEqual()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : <
  // 判斷所有參數是否都小於它後面的參數, 是的話return #t (true), 否則return nil (false)
  static Node IsLessThan( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_cmp = 0 ;       // 作為比較的基準值
    double f_cmp = 0 ;     // 作為比較的基準值
    boolean allInteger = true, allTrue = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (< with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      i_cmp = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_cmp < i_par ) i_cmp = i_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      f_cmp = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_cmp < f_par ) f_cmp = f_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // else
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    
    return ans ;
  } // IsLessThan()
  // -------------------------------------------------------------------------------

  
  
  // -------------------------------------------------------------------------------
  // Function Name : <=
  // 判斷所有參數是否都小於或等於它後面的參數, 是的話return #t (true), 否則return nil (false)
  static Node IsLessThanOrEqual( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_cmp = 0 ;       // 作為比較的基準值
    double f_cmp = 0 ;     // 作為比較的基準值
    boolean allInteger = true, allTrue = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (<= with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      i_cmp = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_cmp <= i_par ) i_cmp = i_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      f_cmp = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_cmp <= f_par ) f_cmp = f_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // else
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    
    return ans ;
  } // IsLessThanOrEqual()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : =
  // 判斷所有參數是否都等於它後面的參數, 是的話return #t (true), 否則return nil (false)
  static Node IsEqualTo( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    int i_cmp = 0 ;       // 作為比較的基準值
    double f_cmp = 0 ;     // 作為比較的基準值
    boolean allInteger = true, allTrue = true ;
    
    // 判斷參數型別
    // 若不是integer也不是float, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "integer" ) ) ;
      else if ( Evaluate.ParameterType( vec.get( i ) ).equals( "float" ) )
        allInteger = false ;
      // 非數字型別的參數
      else throw new MyException( "ERROR (= with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 如果全部的參數都是Integer
    if ( allInteger ) {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      i_cmp = Integer.parseInt( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        int i_par = Integer.parseInt( vec.get( i ).GetToken().PrintToken() ) ;
        if ( i_cmp == i_par ) i_cmp = i_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // if
    
    
    // 其中至少有一個參數是Float
    else {
      // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
      f_cmp = Double.parseDouble( vec.get( 0 ).GetToken().PrintToken() ) ;
      
      // **** i從1開始
      for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
        double f_par = Double.parseDouble( vec.get( i ).GetToken().PrintToken() ) ;
        if ( f_cmp == f_par ) f_cmp = f_par ;  // 此為True, 並把基準值設為後面一個參數
        else allTrue = false ;
      } // for
    } // else
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    
    return ans ;
  } // IsEqualTo()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : atom?
  // 判斷此參數是否為一個atom node, 是的話return #t (true), 否則return nil (false)
  static Node IsAtom( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;    // 參數 parameter
    if ( para != null && para.GetToken() != null && BT.IsATOM( para.GetToken() ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsAtom()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : pair?
  // 判斷此參數是否為一個list(S-exp), 是的話return #t (true), 否則return nil (false)
  static Node IsPair( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "list" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsPair()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : list?
  // 判斷此參數是否為一個pure list(最右邊為nil), 是的話return #t (true), 否則return nil (false)
  static Node IsList( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "list" )
         && Evaluate.IsPureList( para ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsList()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : null?
  // 判斷此參數是否為nil, (), #f
  // 是的話return #t (true), 否則return nil (false)
  static Node IsNull( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "nil" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsNull()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : integer?
  // 判斷此參數的型別是否為integer, 是的話return #t (true), 否則return nil (false)
  static Node IsInteger( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "integer" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsInteger()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : real?  OR  number?
  // 判斷此參數的型別是否為real / number, 是的話return #t (true), 否則return nil (false)
  static Node IsReal_or_Number( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null &&
         ( Evaluate.ParameterType( para ).equals( "integer" ) ||
           Evaluate.ParameterType( para ).equals( "float" ) ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsReal_or_Number()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : string?
  // 判斷此參數的型別是否為string, 是的話return #t (true), 否則return nil (false)
  static Node IsString( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "string" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsString()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : boolean?
  // 判斷此參數的型別是否為boolean, 即為 #t (true) 或者  #f / nil (false)
  // 是的話return #t (true), 否則return nil (false)
  static Node IsBoolean( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null &&
         ( Evaluate.ParameterType( para ).equals( "#t" ) ||
           Evaluate.ParameterType( para ).equals( "nil" ) ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsBoolean()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : symbol?
  // 判斷此參數的型別是否為symbol, 是的話return #t (true), 否則return nil (false)
  static Node IsSymbol( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "symbol" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsSymbol()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : string-append
  // 把所有字串參數接起來成為一個字串, 然後return
  static Node Str_Append( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    String str = "\"" ;     // 先塞入作為開頭的雙引號(")

    // 判斷參數型別
    // 若不是string, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "string" ) ) ;
      // 非字串型別的參數
      else throw new MyException( "ERROR (string-append with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 依序去掉每個參數頭尾的雙引號("), 然後接起來
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      String temp =  vec.get( i ).GetToken().PrintToken() ;
      str = str + temp.substring( 1, temp.length() - 1 ) ;
    } // for
    
    str = str + "\"" ;      // 接上作為結束的雙引號(")
    
    Token t = new Token( str, 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Str_Append()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : string>?
  // 判斷所有字串是否都大於它後面的字串, 是的話return #t (true), 否則return nil (false)
  static Node Str_Greater( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    String s_cmp = "" ;       // 作為比較的基準值
    boolean allTrue = true ;
    
    // 判斷參數型別
    // 若不是string, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "string" ) ) ;
      // 非字串型別的參數
      else throw new MyException( "ERROR (string>? with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
    s_cmp = vec.get( 0 ).GetToken().PrintToken() ;
      
    // **** i從1開始
    for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
      String str = vec.get( i ).GetToken().PrintToken() ;
      // compare結果大於0, 表示s_cmp > str
      if ( s_cmp.compareTo( str ) > 0 ) s_cmp = str ;  // 此為True, 並把基準值設為後面一個參數
      else allTrue = false ;
    } // for
    
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Str_Greater()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : string<?
  // 判斷所有字串是否都小於它後面的字串, 是的話return #t (true), 否則return nil (false)
  static Node Str_Less( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    String s_cmp = "" ;       // 作為比較的基準值
    boolean allTrue = true ;
    
    // 判斷參數型別
    // 若不是string, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "string" ) ) ;
      // 非字串型別的參數
      else throw new MyException( "ERROR (string<? with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
    s_cmp = vec.get( 0 ).GetToken().PrintToken() ;
      
    // **** i從1開始
    for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
      String str = vec.get( i ).GetToken().PrintToken() ;
      // compare結果小於0, 表示s_cmp < str
      if ( s_cmp.compareTo( str ) < 0 ) s_cmp = str ;  // 此為True, 並把基準值設為後面一個參數
      else allTrue = false ;
    } // for
    
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Str_Less()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : string=?
  // 判斷所有字串是否都相等, 是的話return #t (true), 否則return nil (false)
  static Node Str_Equal( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    String s_cmp = "" ;       // 作為比較的基準值
    boolean allTrue = true ;
    
    // 判斷參數型別
    // 若不是string, 就要丟出Error Message
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( Evaluate.ParameterType( vec.get( i ) ).equals( "string" ) ) ;
      // 非字串型別的參數
      else throw new MyException( "ERROR (string=? with incorrect argument type) : "
                                  + BT.PrintLTree( vec.get( i ), 0, false, "" ), 7 ) ;
    } // for
    
    
    // 第一個參數作為一開始比較的基準值, 然後與之後的參數比較
    s_cmp = vec.get( 0 ).GetToken().PrintToken() ;
      
    // **** i從1開始
    for ( int i = 1 ; i < vec.size() && allTrue ; i++ ) {
      String str = vec.get( i ).GetToken().PrintToken() ;
      if ( s_cmp.equals( str ) ) s_cmp = str ;  // 此為True, 並把基準值設為後面一個參數
      else allTrue = false ;
    } // for
    
    
    Token t ;
    if ( allTrue ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Str_Equal()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : eqv?
  // equivalent 等價的
  // 判斷此兩個參數是不是指向同一棵樹, 是的話return #t (true), 否則return nil (false)
  // 若參數同為integer或同為float(即number), 則看是不是等值
  // 若是同為T或同為NIL, 也是return #t (true)
  static Node IsEquivalent( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para1 = vec.get( 0 ) ;
    Node para2 = vec.get( 1 ) ;
    
    if ( para1 == para2 ) t = new Token( "#t", 0, 0 ) ;
    else if ( ( Evaluate.ParameterType( para1 ) == "integer" &&
                Evaluate.ParameterType( para2 ) == "integer" ) || 
              ( Evaluate.ParameterType( para1 ) == "float" &&
                Evaluate.ParameterType( para2 ) == "float" ) ) {
      if ( para1.GetToken().PrintToken().equals( para2.GetToken().PrintToken() ) )
        t = new Token( "#t", 0, 0 ) ;
      else t = new Token( "nil", 0, 0 ) ;
    } // else if
    
    else if ( ( Evaluate.ParameterType( para1 ) == "#t" &&
                Evaluate.ParameterType( para2 ) == "#t" ) || 
              ( Evaluate.ParameterType( para1 ) == "nil" &&
                Evaluate.ParameterType( para2 ) == "nil" ) ) 
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsEquivalent()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : equal?
  // equal 相同的
  // 判斷此兩個參數所binding的樹是不是長一樣, 是的話return #t (true), 否則return nil (false)
  static Node IsEqual( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    String tree1 = BT.PrintLTree( vec.get( 0 ), 0, false, "" ) ;
    String tree2 = BT.PrintLTree( vec.get( 1 ), 0, false, "" ) ;
    
    if ( tree1.equals( tree2 ) ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsEqual()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : not
  // 是 nil的話, return #t (true)
  // 否則return #f / nil (false)
  static Node Not( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "nil" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Not()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : and
  // 依序evaluate參數
  // 只要有一個參數evaluate完的結果為nil, 即跳出迴圈return nil, 不需管後面參數evaluate的結果
  // 若皆為true或可evaluate, 則return最後一個參數evaluate完的結果
  static Node And( Node node ) throws MyException {
    Node ans = new Node(), ans_temp = new Node() ;
    Token t ;
    boolean allTrue = true ;
    
    // -------------------------- 取得還未evaluate的參數 --------------------------
    Vector<Node> vec = new Vector<Node>() ;
    while ( node != null ) {
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else vec.add( node.GetLeft() ) ;
      
      node = node.GetRight() ;
    } // while
    // -------------------------- 取得還未evaluate的參數 --------------------------
    
    for ( int i = 0 ; i < vec.size() && allTrue ; i++ ) {
      try {
        ans_temp = Evaluate.EvalSExp( vec.get( i ), 1 ) ;
      } catch ( MyException e ) {
        // 條件式evaluate完 No return value
        if ( e.GetCase() == 88 ) 
          throw new MyException( "ERROR (unbound condition) : " + 
                                 BT.PrintLTree( vec.get( i ), 0, false, "" ), 22 ) ;
        else throw e ;
      } // catch
      
      // 只要有一個是false, 就return nil (false)
      if ( ans_temp.GetToken() != null && 
           ans_temp.GetToken().GetIntType() == Type.NIL ) {
        t = new Token( "nil", 0, 0 ) ;
        t.Classify() ;
        ans.SetToken( t ) ;
        allTrue = false ;
      } // if
      
      // else ;
      // 非ATOM Node 或者 非nil的ATOM Node 就不做事, 繼續判斷下一個參數
    } // for
      
    if ( allTrue ) 
      ans = Evaluate.EvalSExp( vec.get( vec.size() - 1 ), 1 ) ;

    return ans ;
  } // And()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : or
  // 依序evaluate參數 
  // 只要有一個參數evaluate完的結果為true或可執行
  // 即立刻return 此參數evaluate完的結果, 不需管後面參數evaluate的結果
  // 若皆為nil(false), 則return nil
  static Node Or( Node node ) throws MyException {
    Node ans = new Node(), ans_temp = new Node() ;
    Token t ;
    
    // -------------------------- 取得還未evaluate的參數 --------------------------
    Vector<Node> vec = new Vector<Node>() ;
    while ( node != null ) {
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else vec.add( node.GetLeft() ) ;
      
      node = node.GetRight() ;
    } // while
    // -------------------------- 取得還未evaluate的參數 --------------------------
    
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      try {
        ans_temp = Evaluate.EvalSExp( vec.get( i ), 1 ) ;
      } catch ( MyException e ) {
        // 條件式evaluate完 No return value
        if ( e.GetCase() == 88 ) 
          throw new MyException( "ERROR (unbound condition) : " + 
                                 BT.PrintLTree( vec.get( i ), 0, false, "" ), 22 ) ;
        else throw e ;
      } // catch
      
      // 是ATOM Node又是false, 就不做事, 繼續判斷下一個參數
      if ( ans_temp.GetToken() != null &&
           ans_temp.GetToken().GetIntType() == Type.NIL ) ;
      
      // 非ATOM Node 或者 非nil的ATOM Node就return此參數
      else {
        ans = Evaluate.EvalSExp( vec.get( i ), 1 ) ;
        return ans ;
      } // else
    } // for
    
    
    t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // Or()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : if
  // 如果參數1(條件)evaluate完是 true的話, 再evaluate參數2並return
  // 是false的話, 再evaluate參數3並return
  static Node If( Node node ) throws MyException {
    Node ans = new Node() ;
    
    // -------------------------- 取得還未evaluate的參數 --------------------------
    Vector<Node> vec = new Vector<Node>() ;
    while ( node != null ) {
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else vec.add( node.GetLeft() ) ;
      
      node = node.GetRight() ;
    } // while
    // -------------------------- 取得還未evaluate的參數 --------------------------
    
    Node cond = new Node() ;
    // boolean isTrue = false ;
    try {
      cond = Evaluate.EvalSExp( vec.get( 0 ), 1 ) ;  // evaluate看是true or false
    } catch ( MyException e ) { 
      if ( e.GetCase() == 88 ) 
        throw new MyException( "ERROR (unbound test-condition) : " + 
                               BT.PrintLTree( vec.get( 0 ), 0, false, "" ), 21 ) ;
      else throw e ;
    } // catch
    
    if ( cond.GetToken() != null && cond.GetToken().GetIntType() == Type.NIL ) {
      if ( vec.size() == 2 )
        throw new MyException( "", 88 ) ;

      else ans = Evaluate.EvalSExp( vec.get( 2 ), 1 ) ;
    } // if
    
    else    // 是True 或是 可執行
      ans = Evaluate.EvalSExp( vec.get( 1 ), 1 ) ;
    
    return ans ;
  } // If()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : cond (condition)
  // 一種
  // if (...) ....
  // else if (...) ....
  // else if (...) ....
  // else ....
  // 的概念
  //
  // 先看參數是不是都'非'ATOM Node
  // 然後看每個參數裡面除了conditon還有沒有其他S-exp, 沒有要丟error msg
  // 然後依序判斷每個參數的condition s-exp是不是True或是可執行
  // 1) 如果條件式evaluate完是 true或是可執行的話
  //    就執行(evaluate)後面跟的sub-exp, 並回傳最後一個exp evaluate完的結果
  // 2) 如果條件式evaluate完是false的話, 繼續evaluate下個參數, 重複(1)&(2)步驟
  static Node Cond( Node node ) throws MyException {
    
    Vector<Node> vec = new Vector<Node>() ;  // 第一層所有參數
    Node ans = null ;
    
    // ------------------------------ 取得第一層所有參數 ------------------------------
    while ( node != null ) {  
      // 表示最尾端(結束)
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else {
        // Type須為list(非ATOM Node)
        if ( node.GetLeft().GetToken() != null ) throw new MyException( "", 87 ) ;
        else
          vec.add( node.GetLeft() ) ;
      } // else
      
      node = node.GetRight() ;
    } // while
    // ------------------------------ 取得第一層所有參數 ------------------------------
    
    
    // -------------------------- 判斷第一層參數是否皆為純list -------------------------
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      if ( ! Evaluate.IsPureList( vec.get( i ) ) )
        throw new MyException( "Cond format error_para", 222222 ) ;
    } // for
    // -------------------------- 判斷第一層參數是否皆為純list -------------------------
    

    // ------------------------------ 取得第二層參數個數 ------------------------------

    for ( int i = 0 ; i < vec.size() ; i ++ ) {
      int node_count = 0 ;
      Node temp = vec.get( i ).GetRight() ;
      boolean isEnd = false ;
      for ( Node head = temp ; head != null && ! isEnd ; head = head.GetRight() ) {
        if ( head.GetToken() != null && head.GetToken().GetIntType() == Type.NIL )
          isEnd = true ;
        // 右節點是個ATOM NODE
        else if ( head.GetToken() != null ) throw new MyException( "lalalalala", 9999 ) ;
        else node_count++ ;
      } // for
    
      // 沒有可回傳的值, 丟error msg
      if ( node_count == 0 ) throw new MyException( "", 87 ) ;
    } // for
    // ------------------------------ 取得第二層參數個數 ------------------------------
    
    
    // ------------------------------ 判斷該進哪個條件式 ------------------------------
    Node cond = null ;
    
    for ( int i = 0 ; i < vec.size() ; i++ ) {
        
      // else .... 的情況
      if ( i == vec.size() -1 && vec.get( i ).GetLeft().GetToken() != null
           && vec.get( i ).GetLeft().GetToken().PrintToken().equals( "else" ) ) {

        // -------------------------- 取得evaluate後的第二層參數 --------------------------
        Vector<Node> sub = new Vector<Node>() ;
        Node temp = vec.get( i ).GetRight() ;
        boolean isEnd = false ;
        for ( Node head = temp ; head != null && ! isEnd ; head = head.GetRight() ) {
          if ( head.GetToken() != null && head.GetToken().GetIntType() == Type.NIL )
            isEnd = true ;
          
          // 右節點是個ATOM NODE
          else if ( head.GetToken() != null ) throw new MyException( "lalalalala", 9999 ) ;
          
          // 格式正確 : 要取這個節點
          else sub.add( head.GetLeft() ) ;
        } // for
        // -------------------------- 取得evaluate後的第二層參數 --------------------------
      
        
        // 沒有可回傳的值, 丟error msg
        if ( sub.size() == 0 ) throw new MyException( "", 88 ) ;
        else {
          for ( int k = 0 ; k < sub.size() ; k++ ) {
            try {
              ans = Evaluate.EvalSExp( sub.get( k ), 1 ) ;
            } catch ( MyException e ) {
              if ( e.GetCase() == 88 && k != sub.size() - 1 ) ;
              else throw e ;
            } // catch
          } // for
              
          return ans ;  // for迴圈跑完, ans即是最後一個s-exp執行完的結果
        } // else
      } // if
        
      else cond = Evaluate.EvalSExp( vec.get( i ).GetLeft(), 1 ) ;

        
      // 條件式evaluate結果為nil就不做動作繼續判斷下個參數(條件式)
      if ( cond.GetToken() != null && cond.GetToken().GetIntType() == Type.NIL ) ;
        
      // 表此條件式evaluate結果為True或是可執行
      else {
        // -------------------------- 取得evaluate後的第二層參數 --------------------------
        Vector<Node> sub = new Vector<Node>() ;
        Node temp = vec.get( i ).GetRight() ;
        boolean isEnd = false ;
        for ( Node head = temp ; head != null && ! isEnd ; head = head.GetRight() ) {
          if ( head.GetToken() != null && head.GetToken().GetIntType() == Type.NIL )
            isEnd = true ;
          
          // 右節點是個ATOM NODE
          else if ( head.GetToken() != null ) throw new MyException( "lalalalala", 9999 ) ;
          
          // 格式正確 : 要取這個節點
          else sub.add( head.GetLeft() ) ;
        } // for
        // -------------------------- 取得evaluate後的第二層參數 --------------------------
      
        
        // 沒有可回傳的值, 丟error msg
        if ( sub.size() == 0 ) throw new MyException( "", 88 ) ;
        else {
          for ( int k = 0 ; k < sub.size() ; k++ ) {
            try {
              ans = Evaluate.EvalSExp( sub.get( k ), 1 ) ;
            } catch ( MyException e ) {
              if ( e.GetCase() == 88 && k != sub.size() - 1 ) ;
              else throw e ;
            } // catch
          } // for
              
          return ans ;  // for迴圈跑完, ans即是最後一個s-exp執行完的結果
        } // else
      } // else
      
    } // for
    
    throw new MyException( "", 88 ) ;
    // ------------------------------ 判斷該進哪個條件式 ------------------------------
    
  } // Cond()
  // -------------------------------------------------------------------------------
  
  
  // -------------------------------------------------------------------------------
  // Function Name : begin
  // 每個參數依序evaluate
  // return最後一個參數evaluate完的結果(等於所有事情都做完了)
  static Node Begin( Node node ) throws MyException {
    Vector<Node> vec = new Vector<Node>() ;
    Node ans = new Node() ;
    
    // --------------------- 取未evaluate的 S-exp ---------------------
    while ( node != null ) {
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else vec.add( node.GetLeft() ) ;
      
      node = node.GetRight() ;
    } // while
    // --------------------- 取未evaluate的 S-exp ---------------------
    
    
    // -------------------- evaluate & 執行 S-exp --------------------
    for ( int i = 0 ; i < vec.size() ; i++ ) {
      try {
        ans = Evaluate.EvalSExp( vec.get( i ), 1 ) ;
      } catch ( MyException e ) {
        // evaluate完 no return value
        if ( e.GetCase() == 88 && i != vec.size() - 1 ) ;
        else throw e ;
      } // catch
    } // for
    // -------------------- evaluate & 執行  S-exp --------------------
    
    return ans ;
  } // Begin()
  // -------------------------------------------------------------------------------
  
  
  // -------------------------------------------------------------------------------
  // Function Name : verbose?
  // 看verbose mode是否開啟
  static Node IsVerbose() throws MyException {
    Token t ;
    if ( Main.sIsVerbose ) t = new Token( "#t", 0, 0 ) ;
    else t = new Token( "nil", 0, 0 ) ;

    t.Classify() ;
    return new Node( t ) ;
  } // IsVerbose()
  // -------------------------------------------------------------------------------
  
  
  // -------------------------------------------------------------------------------
  // Function Name : verbose
  // 看傳進來的那個參數是否為nil
  // 如果是nil則關閉verbose mode, 若非nil則開啟verbose mode
  static Node Verbose( Vector<Node> vec ) throws MyException {
    if ( Evaluate.ParameterType( vec.get( 0 ) ).equals( "nil" ) )
      Main.sIsVerbose = false ;
    else Main.sIsVerbose = true ;
    
    return IsVerbose() ;
  } // Verbose()
  // -------------------------------------------------------------------------------
  
  
  // -------------------------------------------------------------------------------
  // Function Name : lambda
  // 定義一次性function
  // format: ( lambda ( zero-or-more-symbols ) one-or-more-S-expressions )
  // format有error即丟"LAMBDA format error"的error msg
  
  // 判斷Lambda format : ( lambda ( zero-or-more-symbols ) one-or-more-S-expressions )
  // 1.判斷第一個參數是否為list or () [nil]
  // 2.第一個參數裡面是否都是symbol
  static Node Lambda( Node node ) throws MyException {
    Token t ;
    Node para1 = node.GetLeft() ;
    Vector<DefObject> f_para = new Vector<DefObject>() ;
    int para_num = 0 ;
    
    // () 無參數的情況********************************************
    if ( para1 != null && para1.GetToken() != null &&
         para1.GetToken().GetIntType() == Type.NIL )
      t = new Token( "#<procedure lambda>", 0, 0 ) ;
    // () 無參數的情況********************************************
    
    // 有參數的情況***********************************************
    // 判斷是不是裡面的東西都是symbol
    else if ( Evaluate.ParameterType( para1 ).equals( "list" ) ) {
      while ( para1 != null ) {
        if ( para1.GetToken() != null && para1.GetToken().GetIntType() == Type.NIL ) ;
        
        // 右節點是個ATOM NODE
        else if ( para1.GetToken() != null ) throw new MyException( "", 87 ) ;
        
        else if ( para1.GetLeft() != null && para1.GetLeft().GetToken() != null 
                  && para1.GetLeft().GetToken().GetIntType() != Type.SYMBOL )
          throw new MyException( "", 87 ) ;
        
        // 第8題隱藏case  ex: ( lambda ( ( cons 1 2 ) x u ) ( + x u ) )
        else if ( para1.GetLeft().GetToken() == null ) throw new MyException( "", 87 ) ;
        
        else {
          para_num ++ ;
          Node nu = null ;
          DefObject p = new DefObject( para1.GetLeft().GetToken().PrintToken(), 
                                       "symbol", nu ) ;
          f_para.add( p ) ;
        } // else
        
        
        para1 = para1.GetRight() ;
      } // while
      
      t = new Token( "#<procedure lambda>", 0, 0 ) ;
    } // else if
    // 有參數的情況***********************************************
    
    else throw new MyException( "", 87 ) ;
    
    
    // ==================== function body ====================
    
    Vector<Node> sxp = new Vector<Node>() ;
    Node para = node.GetRight() ;
    
    while ( para != null ) {
      if ( para.GetToken() != null && para.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( para.GetToken() != null ) throw new MyException( "", 87 ) ;
      
      else sxp.add( para.GetLeft() ) ;
      
      para = para.GetRight() ;
    } // while
    
    // ==================== function body ====================
    
    Main.sLambda_Temp = new DefObject( "lambda", "function_lambda",
                                       new Node( new Token( "#<procedure lambda>", 0, 0 ) ),
                                       para_num, "s-exp", f_para, sxp  ) ;
    
    t.Classify();
    return new Node( t ) ;
  } // Lambda()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : User_Function

  static Node User_Function( Node node, DefObject def ) throws MyException {

    boolean isHavePara = true ;
    Vector<DefObject> vec_para = null ;
    Node e_left = null ;
    
    // 不須傳入參數的function
    if ( def.mFunction_Parameter == null || 
         ( def.mFunction_Parameter != null && def.mFunction_Parameter.isEmpty() ) )
      isHavePara = false ;
    
    // ____________________________ 取得參數(定義區域變數) ____________________________
    
    if ( isHavePara ) vec_para = new Vector<DefObject>() ;
    
    int i = 0 ;
    while ( node != null ) {  
      // 表示最尾端(結束)
      if ( node.GetToken() != null && node.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( node.GetToken() != null ) throw new MyException( "ERROR (non-list) : ", 11 ) ;
      
      else if ( node.GetLeft() != null ) {
        if ( isHavePara ) {
          
          try {
            
            e_left = Evaluate.EvalSExp( node.GetLeft(), 1 ) ;
            
          } catch ( MyException e ) {
            // 參數evaluate完 No return value -> error
            if ( e.GetCase() == 88 ) {
              throw new MyException( "ERROR (unbound parameter) : " + 
                                     BT.PrintLTree( node.GetLeft(), 0, false, "" ), 20 ) ;
            } // if
            else throw e ;
          } // catch
          
          DefObject d = Evaluate.FindDefine( e_left, true ) ;
          String p_name = def.GetFunctionParameter().get( i ).GetName() ;
          
          // 如果參數是 function
          if ( d != null && ( d.mType.equals( "function" ) || d.mType.equals( "function_user" )
                              || d.mType.equals( "function_lambda" ) ) ) {
            
            DefObject p_def  = new DefObject( p_name, d.GetType(), d.GetBinding(), 
                                              d.GetParameterNumber(), d.GetParameterType(), 
                                              d.GetFunctionParameter(), d.GetFunctionBody() ) ;
            vec_para.add( p_def ) ;
          } // if
          
          
          else { // symbol
            DefObject p_def  = new DefObject( p_name, "symbol",  e_left ) ;
            vec_para.add( p_def ) ;
          } // else
          
        } // if
        
        else throw new MyException( "No Para But Input Para", 444444444 ) ;
        
      } // else if
      
      i ++ ;
      node = node.GetRight() ;
    } // while
    
    
    
    if ( isHavePara ) {
      if ( Main.sLocal_Def == null ) Main.sLocal_Def = new Vector<Vec_DefObject>() ;
      Vec_DefObject v = new Vec_DefObject() ;
      v.SetVec( vec_para ) ;
      Main.sLocal_Def.add( v ) ;
    } // if
    // ____________________________ 取得參數(定義區域變數) ____________________________
    
    
    // ____________________________ 執行function body ____________________________
    Node ans = null ;
    for ( int j = 0 ; j < def.mFunction_Body.size() ; j ++ ) {

      try {
        ans = Evaluate.EvalSExp( def.mFunction_Body.get( j ), 1 ) ;
      } catch ( MyException e ) {
        if ( e.GetCase() == 88 && j != def.mFunction_Body.size() - 1 ) ;
        else throw e ;
      } // catch 
      
    } // for
    // ____________________________ 執行function body ____________________________
   

    if ( isHavePara )
      Main.sLocal_Def.remove( Main.sLocal_Def.size() - 1 ) ;   // 處理完這part就pop掉這次定義的參數
    
    return ans ;
  } // User_Function()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : let
  // 定義一次性function跟區域變數 (僅在後面的function body有效)
  // format: ( let '(' { SYMBOL <S-exp> } ')'  one-or-more-S-expressions )
  // format有error即丟"LAMBDA format error"的error msg
  
  // 判斷Let format : ( let '(' { SYMBOL <S-exp> } ')'  one-or-more-S-expressions )
  // 1.判斷第一個參數是否為list or () [nil]
  // 2.第一個參數裡面是否都是'( SYMBOL <S-exp> )'的格式
  
  static Node Let( Node node ) throws MyException {
    Node para1 = node.GetLeft() ;    // 區域變數的定義區
    Vector<DefObject> f_para = new Vector<DefObject>() ;   // 儲存定義完的區域變數
    int para_num = 0 ;
    boolean havePara = true ;
    
    // () 無參數的情況********************************************
    if ( para1 != null && para1.GetToken() != null &&
         para1.GetToken().GetIntType() == Type.NIL ) havePara = false ;
    // () 無參數的情況********************************************
    
    // 有參數的情況***********************************************
    // 判斷是不是裡面的東西都是symbol
    else if ( Evaluate.ParameterType( para1 ).equals( "list" ) ) {
      while ( para1 != null ) {
        if ( para1.GetToken() != null && para1.GetToken().GetIntType() == Type.NIL ) ;
        
        // 右節點是個ATOM NODE
        else if ( para1.GetToken() != null ) throw new MyException( "", 87 ) ;
        
        else {
          Node p = para1.GetLeft() ;   // 取區域變數的定義   ex: (x 5) , (y 4)....
          // System.out.println(BT.PrintLTree(p, 0, false, "")) ;
          
          // 須符合 ( SYMBOL <S-exp> ) 這樣的格式      ex: ( x 5 )
          if ( p != null && p.GetToken() == null && p.GetLeft() != null && 
               p.GetRight() != null && p.GetRight().GetToken() == null ) {
            
            // 不符合list 或是 有超出兩個節點(ex: (x 11 12 )) 
            // 或是 有變數但沒有定義Binding ( p.GetRight().GetLeft() == null )
            if ( ( p.GetRight().GetRight() != null && p.GetRight().GetRight().GetToken() != null
                   && p.GetRight().GetRight().GetToken().GetIntType() != Type.NIL ) ||
                 ( p.GetRight().GetRight() != null && p.GetRight().GetRight().GetToken() == null
                   && p.GetRight().GetRight().GetLeft() != null )
                 || p.GetRight().GetLeft() == null )
              throw new MyException( "", 87 ) ;
            
            Node var_name = p.GetLeft() ;
            Node var_binding = p.GetRight().GetLeft() ;
            
            // 要被定義的區域變數須為SYMBOL
            if ( var_name != null && var_name.GetToken() != null && 
                 var_name.GetToken().GetIntType() == Type.SYMBOL ) {
              
              DefObject isDefined = Evaluate.FindDefine( var_name, true ) ;
              // 名字或Binding相等都算    ex : (let ( (x (read)) (car x))  (3 5) )
              // 要定義的symbol為系統內建function的名字 -> error
              if ( isDefined != null && isDefined.GetType().equals( "function" ) ) 
                throw new MyException( "", 87 ) ;
              
              para_num ++ ;
              DefObject d = new DefObject( var_name.GetToken().PrintToken(), 
                                           "symbol", var_binding ) ;
              f_para.add( d ) ;
              
            } // if
            
            // 定義非SYMBOL的變數 -> error
            else throw new MyException( "", 87 ) ;
          } // if
          
          // 不符合 ( SYMBOL <S-exp> ) 這樣的格式 -> error
          else throw new MyException( "", 87 ) ;
        } // else
 
        para1 = para1.GetRight() ;
      } // while()
    } // else if
    // 有參數的情況***********************************************
    
    else throw new MyException( "", 87 ) ;
    
    
    // 先檢查format再evaluate區域變數的Binding
    for ( int i = 0 ; i < f_para.size() ; i++ ) {
      try {

        f_para.get( i ).SetBinding( Evaluate.EvalSExp( f_para.get( i ).GetBinding(), 1 ) ) ;

      } catch ( MyException e ) {
        // 要定義的binding evaluate完 No return value -> error
        if ( e.GetCase() == 88 ) 
          throw new MyException( "ERROR (no return value) : " + 
                                 BT.PrintLTree( f_para.get( i ).GetBinding(), 0, false, "" ), 23 ) ;
        else throw e ;
      } // catch
    } // for

    
    // 把定義完的區域變數push進stack
    if ( havePara ) {
      if ( Main.sLocal_Def == null ) Main.sLocal_Def = new Vector<Vec_DefObject>() ;
      Vec_DefObject v = new Vec_DefObject() ;

      v.SetVec( f_para ) ;
      Main.sLocal_Def.add( v ) ;
    } // if
    
    // ==================== 取得 function body ====================
    
    Vector<Node> f_body = new Vector<Node>() ;
    Node sxp = node.GetRight() ;         // function body
    
    while ( sxp != null ) {
      if ( sxp.GetToken() != null && sxp.GetToken().GetIntType() == Type.NIL ) ;
      
      // 右節點是個ATOM NODE
      else if ( sxp.GetToken() != null ) throw new MyException( "", 87 ) ;
      
      else f_body.add( sxp.GetLeft() ) ;
      
      sxp = sxp.GetRight() ;
    } // while
    
    // ==================== 取得 function body ====================
    
    
    // ____________________________ 執行function body ____________________________
    Node ans = null ;
    for ( int j = 0 ; j < f_body.size() ; j ++ ) {
      try {
        ans = Evaluate.EvalSExp( f_body.get( j ), 1 ) ;
      } catch ( MyException e ) {
        if ( e.GetCase() == 88 && j != f_body.size() - 1 ) ;
        else throw e ;
      } // catch 
    } // for
    // ____________________________ 執行function body ____________________________
    
    
    if ( havePara )
      Main.sLocal_Def.remove( Main.sLocal_Def.size() - 1 ) ;   // 處理完這part就pop掉這次定義的區域變數
    
    return ans ;
  } // Let()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : read
  // 讀取後面輸入的input (一棵完整的樹)
  static Node Read() throws MyException {
    Node ans = new Node() ;
    
    // ========================== read前的檢查跟字數初始化 ==========================
    // 建好一棵樹後, Column_re要歸零
    // 如果同一行未處理完建樹所用的Line_re變為1
    // 如果處理完或是剩下是White Space或註解則設為0
    if ( GT.BehindIsWSOrComment( Main.sColumn ) )
      Main.sLine_re = 0 ;
    else Main.sLine_re = 1 ;
    Main.sColumn_re = 0 ;
    // ========================== read前的檢查跟字數初始化 ==========================
    
    try {
      ans = BT.ReadSExp( Main.s_oReader ) ;
    } catch( MyException e ) {
      if ( e.GetCase() == 1 || e.GetCase() == 2 || e.GetCase() == 4 || e.GetCase() == 5 ) {
        String s = "\"" + e.getMessage().substring( 0, e.getMessage().length()-1 ) + "\"" ;
        Token t = new Token( s, 0, 0 ) ;
        t.Classify() ;
        t.SetType( Type.ERROR ) ;
        ans = new Node( t ) ;
        
        // syntax error : Error發生的時候,整棵樹要丟掉清空,這行input也不要,line_re跟column也要歸零
        // 這裡檢查跟清空的原因 : 遇到error即丟msg, 導致後面的input沒清掉會讓下次讀s-exp時出錯
        Main.sLine_re = 0 ;
        Main.sColumn_re = 0 ;
        if ( e.GetCase() <= 5 ) {
          Main.sStr = null ;
          Main.sColumn = 0 ;
        } // if  ( syntax error )
        
        return ans ;
      } // if
      else {
        System.out.println( "Read Other Error" ) ;
        throw e ;
      } // else
    } // catch
    
    if ( ! Evaluate.IsPureList( ans ) )
      throw new MyException( "ERROR (non-list) : ", 11 ) ;
    
    return ans ;
  } // Read()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : write
  // 輸出後面跟著的參數 (一棵完整的樹)
  static Node Write( Vector<Node> vec ) throws MyException {
    Node para = vec.get( 0 ) ;
    
    if ( ! Evaluate.IsPureList( para ) )
      throw new MyException( "ERROR (non-list) : ", 11 ) ;
    else {
      String str = BT.PrintLTree( para, 0, false, "" ) ;
      System.out.print( str.substring( 0, str.length()-1 ) ) ;  // 不含換行符號
    } // else

    return para ;
  } // Write()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : eval
  // Evaluate後面跟著的參數
  // 雖然vec傳進來前有被evaluate了, 但要再一次evaluate那個參數
  static Node Eval( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    // level設為0是因為是重新的evaluate, 故為top level
    ans = Evaluate.EvalSExp( vec.get( 0 ), 0 ) ;

    return ans ;
  } // Eval()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : set!
  // 設定參數的意義, parameter1的定義為parameter2
  // 另一個版本的define, 不須一定要在top level
  static Node Set( Node node ) throws MyException {
    // _______________________________ 變數宣告區 _______________________________
    
    Node para1 = node.GetLeft() ;              // 參數1, 被定義的symbol
    Node para2 = node.GetRight().GetLeft() ;   // 參數2, Binding (value)
                                                                                
    DefObject def1 = null, def2 = null, new_def = null ;                        
    Boolean para1_IsDefined = false ;          // 參數1有沒有被定義過 (即sVec_Def存不存在參數1)
    Boolean isDefinedinLocal_Let = false ;
    Boolean isDefinedinParameter = false ;
    Boolean isDefinedinGlobal = false ;
    Boolean isFind = false ;                   // 一找到有被定義過的就出迴圈, 不然當function遞回時會找到更前面的定義
    int stack = -1 ;          // 記錄被定義過且存在於區域的哪層stack

    // _______________________________ 變數宣告區 _______________________________
    
    
    // _______________________________ 處理參數1 _______________________________
    
    // 參數1是Symbol
    if ( para1.GetToken() != null && para1.GetToken().GetIntType() == Type.SYMBOL ) {
      // 如果要定義一個symbol的binding, define後面只接受兩個參數(symbol name & binding)
      // 若有兩個以上的參數即丟出error msg
      if ( node.GetRight().GetRight() != null && node.GetRight().GetRight().GetToken() == null
           && node.GetRight().GetRight().GetLeft() != null )
        throw new MyException( "", 87 ) ;

      // 原本寫法 : def1 = Evaluate.FindDefine( para1, true ) ;
      
      // ==================== 判斷參數1是否被定義過 & 被定義在哪裡 ====================
      
      String name = para1.GetToken().PrintToken() ;
      
      // 如果是執行Let Function 且 有定義區域變數
      if ( Main.sIsLetFunction && Main.sLocal_Def != null && ! Main.sLocal_Def.isEmpty() ) {
        for ( int i = Main.sLocal_Def.size() - 1 ; i > - 1 && ! isFind ; i-- ) {
          for ( int j = 0 ; j < Main.sLocal_Def.get( i ).GetVec().size() ; j++ ) {
            // set : 名字相等才算有找到
            if ( name.equals( Main.sLocal_Def.get( i ).GetVec().get( j ).GetName() ) ) {
              isDefinedinLocal_Let = true ;
              para1_IsDefined = true ;
              stack = i ;
              def1 = Main.sLocal_Def.get( i ).GetVec().get( j ) ;
              isFind = true ;
            } // if
          } // for
        } // for
      } // if
      
      
      // 如果不是執行Let Function 且 有定義參數 
      // lambda 跟 define出來的使用者自定義function, 如果變數在參數中沒有找到定義, 就去全域找
      // 這邊是只在參數找  ( stack最新push進來的一層 )
      else if ( ! Main.sIsLetFunction && Main.sLocal_Def != null && ! Main.sLocal_Def.isEmpty() ) {
        for ( int j = 0 ; j < Main.sLocal_Def.lastElement().GetVec().size() ; j++ ) {
          // set : 名字相等才算有找到
          if ( name.equals( Main.sLocal_Def.lastElement().GetVec().get( j ).GetName() ) ) {       
            isDefinedinParameter = true ;
            para1_IsDefined = true ;
            def1 = Main.sLocal_Def.lastElement().GetVec().get( j ) ;
            isFind = true ;
          } // if
        } // for
      } // else if
      
      
      for ( int i = 0 ; i < Main.sVec_Def.size() ; i++ ) {
        // set : 名字相等才算有找到
        if ( name.equals( Main.sVec_Def.get( i ).GetName() ) ) {    
          isDefinedinGlobal = true ;
          para1_IsDefined = true ;
          def1 = Main.sVec_Def.get( i ) ;
          isFind = true ;
        } // if
      } // for
      // ==================== 判斷參數1是否被定義過 & 被定義在哪裡 ====================
      
    } // if

    // define format error
    else throw new MyException( "", 87 ) ;
    
   
    // ------------------- 看參數1有沒有被定義過 -------------------
    // 被定義過且非內建function
    if ( def1 != null && ! def1.GetType().equals( "function" ) ) {
      para1_IsDefined = true ;
      // 先記著有被定義過,還不能remove掉,因為如果參數2有用到原本的定義就找不到原本的定義了
      // Main.sVec_Def.remove( def1 ) ;
    } // if

    // 被定義過且為內建function
    else if ( def1 != null && def1.GetType().equals( "function" ) )
      // 參數1為系統內建Function
      throw new MyException( "", 87 ) ;
    
    // def1 == null
    else ;
    // ------------------- 看參數1有沒有被定義過 -------------------
    // _______________________________ 處理參數1 _______________________________
    
    
    // _______________________________ 處理參數2 _______________________________
    
    Node para2_eval = new Node() ;
    try {
      para2_eval = Evaluate.EvalSExp( para2, 1 ) ;
    } catch ( MyException e ) {
      // 要定義的binding evaluate完 No return value -> error
      if ( e.GetCase() == 88 ) 
        throw new MyException( "ERROR (no return value) : " + 
                               BT.PrintLTree( para2, 0, false, "" ), 23 ) ;
      else throw e ;
    } // catch
    
    if ( para2_eval.GetToken() != null ) {
      def2 = Evaluate.FindDefine( para2_eval, false ) ;
      // 為了處理 (define x (quote a)) , 所以FindDefine參數傳false
      // 要binding跟token一樣才算被定義過, 名字跟token一樣 不用去找它的binding
      // 因為 'a 就像 'cons 一樣, 就算 cons 看起來是有定義過, 但事實上不是 binding 到 #<procedure cons>
    } // if
      

    
    // 參數2是除了symbol外的atom node (int,float,string,#t,nil)
    // 不需要管def2有沒有找到, 不然 (define a 5 ) (define b a ) 這個case def2為非null
    // 直接binding
    if ( para2_eval.GetToken() != null && Evaluate.IsATOM( para2_eval.GetToken() ) ) {
      // System.out.println("fdfd") ;
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    } // if
    
    
    // Define + Lambda
    else if ( para2_eval.GetToken() != null &&
              para2_eval.GetToken().PrintToken().equals( "#<procedure lambda>" ) ) {
      
      // (define x (lambda ( a b ) ( + a 5 ) ( + b 5 )))
      if ( def2 == null ) {
        new_def = Main.sLambda_Temp ;
        new_def.SetName( para1.GetToken().PrintToken() ) ;
      } // if
      
      // (define x (lambda ( a b ) ( + a 5 ) ( + b 5 )))
      // (define y x )
      else {
        new_def = new DefObject( para1.GetToken().PrintToken(), def2.GetType(), def2.GetBinding(), 
                                 def2.GetParameterNumber(), def2.GetParameterType(), 
                                 def2.GetFunctionParameter(), def2.GetFunctionBody() ) ;
      } // else  
    } // else if
    
    // 參數2是經過quote處理過的symbol atom node
    // ex : (define a 'hi)
    else if ( para2_eval.GetToken() != null && def2 == null
              && para2_eval.GetToken().GetIntType() == Type.SYMBOL )
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    
    // 參數2是非ATOM Node 且 沒被定義過  ( 如 : (1 . 2) )
    // 直接binding
    else if ( para2_eval.GetToken() == null && def2 == null )
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    
    // 參數2是被定義過的function
    // 直接binding (defObj的type為function_user)
    else if ( def2 != null && ( def2.GetType().equals( "function" ) || 
                                def2.GetType().equals( "function_user" ) ) ) {
      new_def = new DefObject( para1.GetToken().PrintToken(), "function_user", 
                               def2.GetParameterNumber(), def2.GetParameterType() ) ;
      new_def.SetBinding( def2.GetBinding() ) ;
    } // else if
    
    
    // 參數2是被定義過的function且為使用者自己定義的function
    // 直接binding (defObj的type為function_lambda)
    else if ( def2 != null && def2.GetType().equals( "function_lambda" ) ) {
      new_def = new DefObject( para1.GetToken().PrintToken(),
                               "function_lambda", def2.GetBinding(), 
                               def2.GetParameterNumber(), def2.GetParameterType(),
                               def2.GetFunctionParameter(), def2.GetFunctionBody() ) ;
    } // else if
    
    
    // ERROR Object
    else if ( para2_eval.GetToken() != null && para2_eval.GetToken().GetIntType() == Type.ERROR ) {
      new_def = new DefObject( para1.GetToken().PrintToken(), "symbol", para2_eval ) ;
    } // if
    
    
    // ************************
    else throw new MyException( "No Binding", 2222 ) ;
    // ************************
    
    // _______________________________ 處理參數2 _______________________________
    
    
    // _______________________________ 處理回傳值  _______________________________
    if ( isDefinedinParameter && para1_IsDefined ) {
      Main.sLocal_Def.lastElement().GetVec().remove( def1 ) ;
      Main.sLocal_Def.lastElement().GetVec().add( new_def ) ;
    } // if
    
    else if ( isDefinedinLocal_Let && para1_IsDefined ) {
      Main.sLocal_Def.get( stack ).GetVec().remove( def1 ) ;
      Main.sLocal_Def.get( stack ).GetVec().add( new_def ) ;
    } // else if
    
    else if ( isDefinedinGlobal && para1_IsDefined ) {
      Main.sVec_Def.remove( def1 ) ;
      Main.sVec_Def.add( new_def ) ;
    } // else if

    else Main.sVec_Def.add( new_def ) ;
    
      
    return para2_eval ;
    // _______________________________ 處理回傳值  _______________________________

  } // Set()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : create-error-object
  // 創建一個Error-Object
  // 參數型別只接受String, 其餘則丟出error msg
  static Node C_Error_Object( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    if ( ! Evaluate.ParameterType( vec.get( 0 ) ).equals( "string" ) )
      throw new MyException( "ERROR (create-error-object with incorrect argument type) : "
                             + BT.PrintLTree( vec.get( 0 ), 0, false, "" ), 7 ) ;
    else {
      vec.get( 0 ).GetToken().SetType( Type.ERROR ) ;
      ans = vec.get( 0 ) ;
    } // else
      
    return ans ;
  } // C_Error_Object()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : error-object?
  // 判斷參數是否為error-object, 是則回傳 #t (true), 否則回傳 nil (false)
  static Node IsErrorObject( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Token t ;
    Node para = vec.get( 0 ) ;
    if ( para != null && Evaluate.ParameterType( para ).equals( "error" ) )
      t = new Token( "#t", 0, 0 ) ;
    
    else t = new Token( "nil", 0, 0 ) ;
    
    t.Classify() ;
    ans.SetToken( t ) ;
    return ans ;
  } // IsErrorObject()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : display-string
  // 判斷參數是否為string 或 error-object, 否則丟error msg
  // print 不含前後雙引號的token內容, 並return參數node
  static Node Display_String( Vector<Node> vec ) throws MyException {
    Node ans = new Node() ;
    Node para = vec.get( 0 ) ;
    if ( ! Evaluate.ParameterType( para ).equals( "string" ) &&
         ! Evaluate.ParameterType( para ).equals( "error" ) )
      throw new MyException( "ERROR (display-string with incorrect argument type) : "
                             + BT.PrintLTree( para, 0, false, "" ), 7 ) ;

    else {
      String str = para.GetToken().PrintToken() ;
      System.out.print( str.substring( 1, str.length()-1 ) ) ;   // 去掉前後雙引號
      ans = para ;
    } // else
    
    return ans ;
  } // Display_String()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : newline
  // print '\n' , 並return nil
  static Node NewLine() throws MyException {
    System.out.println() ;
    
    Token t ;
    t = new Token( "nil", 0, 0 ) ;
    t.Classify() ;
    return new Node( t ) ;
  } // NewLine()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : symbol->string
  // 只接受symbol參數, 用以string型別的方式return參數
  static Node SymbolToString( Vector<Node> vec ) throws MyException {
    Node para = vec.get( 0 ) ;
    if ( para != null && ! Evaluate.ParameterType( para ).equals( "symbol" ) )
      throw new MyException( "ERROR (symbol->string with incorrect argument type) : "
                             + BT.PrintLTree( para, 0, false, "" ), 7 ) ;
    Token t ;
    String str = "\"" + para.GetToken().PrintToken() + "\"" ;    // 加上前後雙引號變成string
    t = new Token( str, 0, 0 ) ;
    t.Classify() ;
    return new Node( t ) ;
  } // SymbolToString()
  // -------------------------------------------------------------------------------
  
  
  
  // -------------------------------------------------------------------------------
  // Function Name : number->string
  // 只接受symbol參數, 用以string型別的方式return參數
  static Node NumberToString( Vector<Node> vec ) throws MyException {
    Node para = vec.get( 0 ) ;
    if ( ! Evaluate.ParameterType( para ).equals( "integer" ) &&
         ! Evaluate.ParameterType( para ).equals( "float" ) )
      throw new MyException( "ERROR (number->string with incorrect argument type) : "
                             + BT.PrintLTree( para, 0, false, "" ), 7 ) ;
    Token t ;
    String str = "\"" + para.GetToken().PrintToken() + "\"" ;    // 加上前後雙引號變成string
    t = new Token( str, 0, 0 ) ;
    t.Classify() ;
    return new Node( t ) ;
  } // NumberToString()
  // -------------------------------------------------------------------------------
  
  
} // class MyFunctions
