package org.beginningee6.book.chapter07.ejb.ex06;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;

/**
 * SessionContextにアクセスし、明示的にトランザクションをロールバックする
 * 例を示すためのステートレス・セッションBean。
 * 
 * SessionContextは、＠Resourceアノテーションを付与することで、
 * EJBコンテナにより自動的に注入される。
 *
 */
@Stateless
@Local(ItemEJBLocal.class)
@Remote(ItemEJBRemote.class)
@LocalBean
public class ItemEJB implements ItemEJBLocal, ItemEJBRemote {

	@PersistenceContext(unitName = "Chapter07ProductionPU")
	private EntityManager em;

	// EJBコンテナによりSessionContextを注入
	@Resource
	private SessionContext context;

	/**
	 * Book01エンティティを永続化する。
	 * 
	 * ただし、エンティティのtitleフィールドが
	 * "Book 1 Title" であった場合はSessionContext.setRollbackOnly()
	 * によりトランザクションをロールバックにマークして
	 * CannotCreateBookException をスローする。
	 */
	public Book01 createBook(Book01 book) throws CannotCreateBookException {
		
		// エンティティを永続化
        em.persist(book);

         if (book.getTitle().equals("Book 1 Title")) {
             // わざとトランザクションをロールバックにマークする。
        	 // 
             // このifブロックが無く、単に
             // context.setRollbackOnly();
             // return book;
             // とした場合は、setRollbackOnly()呼び出し時に例外がスロー
             // されることなく、return bookに進みメソッドの実行が終了する。
             // このメソッドの終了時点でトランザクションがロールバックされる。
             // （つまり、bookエンティティがデータベース上では永続化されない状態になる）
        	 // 
             // 結果として、このメソッドの呼び出し側にはbookエンティティが永続化
             // されていないにも関わらずIDが付番されたbookオブジェクトが返されてしまう。
        	 
             // 従って、トランザクションをロールバックにマークする場合は、何らかの
        	 // チェック例外かトランザクションをロールバックにマークする機能を持つ
        	 // 非チェック例外をスローして呼び出し側に通知すべきであると考える。）

        	 context.setRollbackOnly();

        	 // チェック例外をスロー
        	 throw new CannotCreateBookException();
        }
        
        return book;
	}
}
