package org.beginningee6.book.chapter07.ejb.ex05;

import java.util.List;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * ステートレス・セッションBeanのローカルインタフェースとして
 * 機能させることを意図したインタフェース。
 * 
 * このインタフェースで＠Localインタフェースを付与する代わりに、
 * BeanクラスであるItemEJBで
 * 
 * >> ＠Local(ItemEJBLocal.class)
 * 
 * をクラス宣言に付与することで、このインタフェースがローカル
 * インタフェースとしてBeanクラスに結び付けられる。
 */
public interface ItemEJBLocal {
	List<Book01> findBooks();
	Book01 createBook(Book01 book);
	
	List<CD01> findCDs();
	CD01 createCD(CD01 cd);
}
