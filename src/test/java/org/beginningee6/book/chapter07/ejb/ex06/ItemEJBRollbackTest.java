package org.beginningee6.book.chapter07.ejb.ex06;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * SessionContextにアクセスして明示的にトランザクションをロールバックする
 * EJBの動作を確認するテスト。
 * 
 * テスト対象のステートレス・セッションBeanの
 * createBook()メソッドに渡すBook01エンティティの
 * titleフィールドが"Book 1 Title"の場合は
 * ロールバックにマークして、例外をスローする
 * ようになっている。
 * （結果として、Book01エンティティの永続化は行われない）
 */
@RunWith(Arquillian.class)
public class ItemEJBRollbackTest {
	
	private static final Logger logger = Logger.getLogger(ItemEJBRollbackTest.class
			.getName());

	@Deployment
	public static Archive<?> createDeployment() {
		File[] dependencyLibs 
			= Maven
				.configureResolver()				
				.fromFile("D:\\apache-maven-3.0.3\\conf\\settings.xml")
//				.fromFile("C:\\Maven\\apache-maven-3.0.5\\conf\\settings.xml")
				.resolve("org.beginningee6.book:beginningee6-chapter07-jpa:0.0.1-SNAPSHOT")
				.withTransitivity()
				.asFile();

		WebArchive archive = ShrinkWrap
				.create(WebArchive.class)
				.addPackage(ItemEJB.class.getPackage())
				.addAsLibraries(dependencyLibs)
				.addAsWebInfResource("jbossas-ds.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction userTransaction;

	@EJB
	ItemEJB itemEJB;				// インターフェースなしで使用するEJBの注入
	
	@EJB
	ItemEJBLocal itemEJBLocal;		// ローカルインターフェースで使用するEJBの注入
	
	@EJB
	ItemEJBRemote itemEJBRemote;	// リモートインターフェースで使用するEJBの注入

	@Before
	public void setUp() throws Exception {
		clearData();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Book01").executeUpdate();
		userTransaction.commit();
	}

	/**
	 * titleを"Book 1 Title"に設定したBook01エンティティを永続化する。
	 * 永続化に当たっては、インタフェースなしのEJBに対してメソッドを
	 * 実行する。
	 * 
	 * この条件でcreateBook()メソッドを呼び出すと、例外がスローされる。
	 * createBook()メソッドによるBook01エンティティの映像化はロールバックされる。
	 */
	@Test
	public void testCreateABookViaNoInterfaceView() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

        ///// テスト /////
        
		try {
			// インターフェースなしでEJBを使用
			@SuppressWarnings("unused")
			Book01 returned = itemEJB.createBook(book);
			
			fail("Should throw exception");
		} catch (Exception e) {
			// CannotCreateBookExceptionが発生
			assertThat(e, is(instanceOf(CannotCreateBookException.class)));
		}
        
        ///// 検証 /////
        
		// データ登録されていない（ロールバックされている）
		TypedQuery<Book01> query = em.createNamedQuery("Book01.findAllBooks", Book01.class);
		List<Book01> persisted = query.getResultList();
		assertThat(persisted.size(), is(0));
	}

	/**
	 * titleを"Book 1 Title"に設定したBook01エンティティを永続化する。
	 * 永続化に当たっては、ローカルインタフェースで使用するEJBに対して
	 * メソッドを実行する。
	 * 
	 * この条件でcreateBook()メソッドを呼び出すと、例外がスローされる。
	 * createBook()メソッドによるBook01エンティティの映像化はロールバックされる。
	 */
	@Test
	public void testCreateABookViaLocalInterface() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

        ///// テスト /////
        
		try {
			// ローカルインターフェースでEJBを使用
			@SuppressWarnings("unused")
			Book01 returned = itemEJBLocal.createBook(book);
			
			fail("Should throw exception");
		} catch (Exception e) {
			// CannotCreateBookExceptionが発生
			assertThat(e, is(instanceOf(CannotCreateBookException.class)));
		}
        
        ///// 検証 /////
        
		// データは登録されていない（ロールバックされている）
		TypedQuery<Book01> query = em.createNamedQuery("Book01.findAllBooks", Book01.class);
		List<Book01> persisted = query.getResultList();
		assertThat(persisted.size(), is(0));
	}
	
	/**
	 * titleを"Book 1 Title"に設定したBook01エンティティを永続化する。
	 * 永続化に当たっては、リモートインタフェースで使用するEJBに対して
	 * メソッドを実行する。
	 * 
	 * この条件でcreateBook()メソッドを呼び出すと、例外がスローされる。
	 * createBook()メソッドによるBook01エンティティの映像化はロールバックされる。
	 */
	@Test
	public void testCreateABookViaRemoteInterface() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

        ///// テスト /////
        
		try {
			// リモートインターフェースでEJBを使用
			@SuppressWarnings("unused")
			Book01 returned = itemEJBRemote.createBook(book);
			
			fail("Should throw exception");
		} catch (Exception e) {
			// CannotCreateBookExceptionが発生
			assertThat(e, is(instanceOf(CannotCreateBookException.class)));
		}
        
        ///// 検証 /////
        
		// データは登録されていない（ロールバックされている）
		TypedQuery<Book01> query = em.createNamedQuery("Book01.findAllBooks", Book01.class);
		List<Book01> persisted = query.getResultList();
		assertThat(persisted.size(), is(0));
	}

}
