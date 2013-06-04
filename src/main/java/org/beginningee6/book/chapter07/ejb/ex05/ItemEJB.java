package org.beginningee6.book.chapter07.ejb.ex05;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * 
 *  ローカル・インターフェース、
 * リモート・インターフェース、
 * 及びインターフェースなしで公開される
 * ステートレス・セッションBeanのテスト。
 * 
 * パッケージorg.beginningee6.book.chapter07.ejb.ex04
 * のEJBと機能とクライアントからのアクセス方法の点では
 * 等価であるが、インタフェースの宣言に関して以下の点が異なっている。
 * 
 * ・インタフェースItemEJBLocalで＠Localアノテーションを
 * 　付加せずに、このBeanクラスで＠Localアノテーションを
 * 　インタフェース名とともに付加することで、ローカル
 * 　インタフェースの宣言を行っている。
 * ・インタフェースItemEJBLocalで＠Remoteアノテーションを
 * 　付加せずに、このBeanクラスで＠Remoteアノテーションを
 * 　インタフェース名とともに付加することで、リモート
 * 　インタフェースの宣言を行っている。
 * 
 */
@Stateless
@Local(ItemEJBLocal.class)		// ローカルインタフェースを宣言
@Remote(ItemEJBRemote.class)	// リモートインタフェースを宣言
@LocalBean
public class ItemEJB implements ItemEJBLocal, ItemEJBRemote {

	// エンティティのデータアクセス用にEntity Managerを注入
	@PersistenceContext(unitName = "Chapter07ProductionPU")
	private EntityManager em;

	/**
	 * 名前付きクエリを用いて、永続化されているすべての
	 * Book01エンティティを取得する
	 * 
	 * ⇒ローカルインタフェース、リモートインタフェース、インタフェース無しで公開
	 * 
	 * @return 永続化されているすべてのBook01エンティティのリスト
	 */
	public List<Book01> findBooks() {
		TypedQuery<Book01> query = em.createNamedQuery("Book01.findAllBooks", Book01.class);
		
		return query.getResultList();
	}

    /**
     * 新規にBook01エンティティを永続化する
     * 
     * ⇒ローカルインタフェース、インタフェース無しで公開
     * 
     * @param book 永続化するBook01エンティティ
     * @return 永続化された状態の（＝主キーが付番された）Book01エンティティ
     */
	public Book01 createBook(Book01 book) {
        em.persist(book);

        return book;
	}

	/**
	 * 名前付きクエリを用いて、永続化されているすべての
	 * CD01エンティティを取得する
	 * 
	 * ⇒ローカルインタフェース、リモートインタフェース、インタフェース無しで公開
	 * 
	 * @return 永続化されているすべてのCD01エンティティのリスト
	 */
	public List<CD01> findCDs() {
		TypedQuery<CD01> query = em.createNamedQuery("CD01.findAllCDs", CD01.class);
		
		return query.getResultList();
	}

    /**
     * 新規にCD01エンティティを永続化する
     * 
     * ⇒ローカルインタフェース、インタフェース無しで公開
     * 
     * @param cd 永続化するCD01エンティティ
     * @return 永続化された状態の（＝主キーが付番された）CD01エンティティ
     */
	public CD01 createCD(CD01 cd) {
        em.persist(cd);

        return cd;
	}
}
