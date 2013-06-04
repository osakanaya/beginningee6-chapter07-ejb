package org.beginningee6.book.chapter07.ejb.ex06;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;

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
	Book01 createBook(Book01 book) throws CannotCreateBookException;
}
