package org.beginningee6.book.chapter07.ejb.ex04;

import java.util.List;

import javax.ejb.Local;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * ＠Localアノテーションが付与されたローカルインターフェース。
 * 
 * このインタフェースを介してローカル呼び出しによりアクセスする
 * クライアントは、このインタフェースで定義されたメソッドのみを
 * 実行することができる。
 */
@Local
public interface ItemEJBLocal {
	List<Book01> findBooks();
	Book01 createBook(Book01 book);
	
	List<CD01> findCDs();
	CD01 createCD(CD01 cd);
}
