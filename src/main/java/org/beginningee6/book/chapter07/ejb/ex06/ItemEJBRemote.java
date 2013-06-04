package org.beginningee6.book.chapter07.ejb.ex06;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;

/**
 * ステートレス・セッションBeanのリモートインタフェースとして
 * 機能させることを意図したインタフェース。
 * 
 * このインタフェースで＠Remoteインタフェースを付与する代わりに、
 * BeanクラスであるItemEJBで
 * 
 * >> ＠Remote(ItemEJBRemote.class)
 * 
 * をクラス宣言に付与することで、このインタフェースがリモート
 * インタフェースとしてBeanクラスに結び付けられる。
 */
public interface ItemEJBRemote {
	Book01 createBook(Book01 book) throws CannotCreateBookException;
}
