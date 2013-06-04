package org.beginningee6.book.chapter07.ejb.ex01;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;

/**
 * 
 * JPAを利用してBook01エンティティおよびCD01エンティティの
 * データアクセスを処理するステートレス・セッションBean
 * 
 */
@Stateless	// ステートレス・セッションBeanであることを宣言する
public class ItemEJB {

	// エンティティのデータアクセス用にEntity Managerを注入
	@PersistenceContext(unitName = "Chapter07ProductionPU")
	private EntityManager em;
	
	/**
	 * 名前付きクエリを用いて、永続化されているすべての
	 * Book01エンティティを取得する
	 * 
	 * @return 永続化されているすべてのBook01エンティティのリスト
	 */
	public List<Book01> findBooks() {
		TypedQuery<Book01> query = em.createNamedQuery("Book01.findAllBooks", Book01.class);
		
		return query.getResultList();
	}
	
	/**
	 * 主キーを指定して永続化されている特定のBook01エンティティを
	 * 取得する
	 * @param id 取得するエンティティの主キー
	 * @return 指定した主キーを持つBook01エンティティ
	 */
    public Book01 findBookById(Long id) {
        return em.find(Book01.class, id);
    }

    /**
     * 新規にBook01エンティティを永続化する
     * @param book 永続化するBook01エンティティ
     * @return 永続化された状態の（＝主キーが付番された）Book01エンティティ
     */
    public Book01 createBook(Book01 book) {
        em.persist(book);

        return book;
    }

    /**
     * 永続化されているBook01エンティティをデータベースから削除する
     * @param book 削除するBook01エンティティ
     */
    public void deleteBook(Book01 book) {
        em.remove(em.merge(book));
    }

    /**
     * Book01エンティティのフィールドの値を更新する
     * @param book フィールドの更新が反映された
     * （しかし、まだそれがデータベースに反映されていない）
     * Book01エンティティ
     * @return フィールドの更新がデータベースへ反映されたBook01エンティティ
     */
    public Book01 updateBook(Book01 book) {
        
    	return em.merge(book);
    }

	/**
	 * 名前付きクエリを用いて、永続化されているすべての
	 * CD01エンティティを取得する
	 * 
	 * @return 永続化されているすべてのCD01エンティティのリスト
	 */
    public List<CD01> findCDs() {
		TypedQuery<CD01> query = em.createNamedQuery("CD01.findAllCDs", CD01.class);
		
		return query.getResultList();
	}
	
	/**
	 * 主キーを指定して永続化されている特定のCD01エンティティを
	 * 取得する
	 * @param id 取得するエンティティの主キー
	 * @return 指定した主キーを持つCD01エンティティ
	 */
    public CD01 findCDById(Long id) {
        return em.find(CD01.class, id);
    }

    /**
     * 新規にCD01エンティティを永続化する
     * @param cd 永続化するCD01エンティティ
     * @return 永続化された状態の（＝主キーが付番された）CD01エンティティ
     */
    public CD01 createCD(CD01 cd) {
        em.persist(cd);
        
        return cd;
    }

    /**
     * 永続化されているCD01エンティティをデータベースから削除する
     * @param cd 削除するCD01エンティティ
     */
    public void deleteCD(CD01 cd) {
        em.remove(em.merge(cd));
    }

    /**
     * CD01エンティティのフィールドの値を更新する
     * @param cd フィールドの更新が反映された
     * （しかし、まだそれがデータベースに反映されていない）
     * CD01エンティティ
     * @return フィールドの更新がデータベースへ反映されたCD01エンティティ
     */
    public CD01 updateCD(CD01 cd) {
        
    	return em.merge(cd);
    }
}
