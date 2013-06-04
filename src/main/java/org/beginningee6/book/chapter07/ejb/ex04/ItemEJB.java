package org.beginningee6.book.chapter07.ejb.ex04;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * ローカル・インターフェース、
 * リモート・インターフェース、
 * 及びインターフェースなしで公開される
 * ステートレス・セッションBeanのテスト。
 * 
 * ＠Localアノテーションを付したItemEJBLocalインターフェース
 * を実装して、それらのメソッドをローカル呼び出しするクライアントに公開。
 * クライアント側では以下の様に注入する。
 * （このように注入されるEJBのインスタンスは「ローカルインタフェースによって
 * 公開されるEJBのビュー」とも呼ばれる）
 * 
 * >> ＠EJB ItemEJBLocal itemEJBLocal;
 * 
 * ＠Remoteアノテーションを付したItemEJBRemoteインターフェース
 * を実装して、それらのメソッドをリモート呼び出しするクライアントに公開。
 * クライアント側では以下の様に注入する。
 * （このように注入されるEJBのインスタンスは「リモートインタフェースによって
 * 公開されるEJBのビュー」とも呼ばれる）
 * 
 * >> ＠EJB ItemEJBRemote itemEJBRemote;
 * 
 * ＠LocalBeanアノテーションにより、
 * publicで宣言されるすべてのメソッドをインターフェースなしで
 * ローカル呼び出しするクライアントに公開。
 * クライアント側では以下の様に注入する。
 * （このように注入されるEJBのインスタンスは「インターフェースの無い
 * EJBのビュー（No-Interface View）」とも呼ばれる）
 * 
 * >> ＠EJB ItemEJB itemEJB;
 * 
 * ローカルまたはリモート・インターフェースを実装し、かつ、
 * ＠LocalBeanアノテーションを付与しないEJBに対して、クライアント側で
 * 
 * >> ＠EJB ItemEJB itemEJB;
 * 
 * の様に注入しようとするとデプロイ時に例外が発生する。
 * 
 */
@Stateless	// ステートレス・セッションBeanであることを宣言
@LocalBean	// インタフェース無しでローカル呼び出しする
			// クライアントにすべてのpublicメソッドを公開
public class ItemEJB implements ItemEJBLocal, ItemEJBRemote {	// ローカルとリモートインタフェースを実装

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
