package org.beginningee6.book.chapter07.ejb.ex04;

import java.util.List;

import javax.ejb.Remote;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * ＠Remoteアノテーションが付与されたリモートインターフェース。
 * 
 * このインタフェースを介してリモート呼び出しによりアクセスする
 * クライアントは、このインタフェースで定義されたメソッドのみを
 * 実行することができる。
 */
@Remote
public interface ItemEJBRemote {
	List<Book01> findBooks();
	
	List<CD01> findCDs();
}
