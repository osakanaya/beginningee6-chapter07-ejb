package org.beginningee6.book.chapter07.ejb.ex01;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter07.ejb.ex01.ItemEJB;
import org.beginningee6.book.chapter07.jpa.ex01.Book01;
import org.beginningee6.book.chapter07.jpa.ex01.CD01;
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
 * ステートレス・セッションBeanのテスト。
 * 
 * このテストでは、ローカル呼び出しにより
 * EJBが直接公開したすべてのメソッドを実行して
 * その結果を検証している。
 * 
 * ステートレス・セッションBeanとして定義された
 * ItemEJBクラスのオブジェクトは、＠EJBアノテーションの
 * 機能を利用してアプリケーションサーバにより自動的に
 * 注入される。

 */
@RunWith(Arquillian.class)
public class ItemEJBTest {
	private static final Logger logger = Logger.getLogger(ItemEJBTest.class
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
	ItemEJB itemEJB;	// ステートレス・セッションBeanの注入

	@Before
	public void setUp() throws Exception {
		clearData();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Book01").executeUpdate();
		em.createQuery("DELETE FROM CD01").executeUpdate();
		userTransaction.commit();
	}

	/**
	 * Book01エンティティを永続化するテスト。
	 */
	@Test
	public void testCreateABook() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

        ///// テスト /////
        
		// ItemEJBを通じてBook01エンティティを永続化
		Book01 returned = itemEJB.createBook(book);
        
        ///// 検証 /////
        
		assertThat(returned.getId(), is(notNullValue()));

		// Book01エンティティが永続化できているかどうかを
		// 直接データベースから確認
		Book01 persisted = em.find(Book01.class, returned.getId());
		assertThat(persisted, is(returned));
	}

	/**
	 * 主キーを指定して永続化されたBook01エンティティを取得するテスト。
	 */
	@Test
	public void testFindABookById() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

		Book01 created = itemEJB.createBook(book);

        ///// テスト /////
        
		// ItemEJBを通じてBook01エンティティを取得
		Book01 found = itemEJB.findBookById(created.getId());
        
        ///// 検証 /////
        
		assertThat(found, is(created));
	}

	/**
	 * 永続化されたBook01エンティティをデータベース上から
	 * 削除するテスト。
	 */
	@Test
	public void testDeleteABook() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

		Book01 created = itemEJB.createBook(book);

        ///// テスト /////
        
		// ItemEJBを通じてBook01エンティティを削除
		itemEJB.deleteBook(created);
        
        ///// 検証 /////
        
		// Book01エンティティが削除されていることを確認
		Book01 found = itemEJB.findBookById(created.getId());

		assertThat(found, is(nullValue()));
	}

	/**
	 * 永続化されたBook01エンティティのフィールド値を更新するテスト。
	 */
	@Test
	public void testUpdateABook() throws Exception {
		
		///// 準備 /////
		
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

		Book01 created = itemEJB.createBook(book);

        ///// テスト /////

		// ItemEJBを通じてBook01エンティティを更新
		created.setTitle("Book 1 Title - Updated");
		Book01 updated = itemEJB.updateBook(created);
        
        ///// 検証 /////
        
		assertThat(updated.getTitle(), is("Book 1 Title - Updated"));
		
		// Book01エンティティのフィールド値が更新されていることを確認
		Book01 found = itemEJB.findBookById(created.getId());
		assertThat(found, is(updated));
	}

	/**
	 * 複数のBook01エンティティが永続化されている状態で、
	 * これらすべてのエンティティが取得できることを確認する
	 */
	@Test
	public void testFindAllBooks() throws Exception {
		
		///// 準備 /////
		
		Book01 book1 = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);

		Book01 book2 = new Book01(
				"Book 2 Title", 
				20.0F, 
				"Book 2 Description", 
				"2-22222-222-2", 
				222, 
				true);

		Book01 created1 = itemEJB.createBook(book1);
		Book01 created2 = itemEJB.createBook(book2);

        ///// テスト /////
        
		// ItemEJBを通じてすべての永続化されたBook01エンティティを取得
		List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
		assertThat(found.size(), is(2));
		assertThat(found, hasItems(created1, created2));
	}
	
	/**
	 * CD01エンティティを永続化するテスト。
	 */
	@Test
	public void testCreateACD() throws Exception {
		
		///// 準備 /////
		
        CD01 cd = new CD01(
        		"CD 1 Title",
        		10.0F,
        		"CD 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");

        ///// テスト /////
        
		// ItemEJBを通じてCD01エンティティを永続化
		CD01 returned = itemEJB.createCD(cd);
        
        ///// 検証 /////
        
		assertThat(returned.getId(), is(notNullValue()));

		// CD01エンティティが永続化できているかどうかを
		// 直接データベースから確認
		CD01 persisted = em.find(CD01.class, returned.getId());
		assertThat(persisted, is(returned));
	}

	/**
	 * 主キーを指定して永続化されたCD01エンティティを取得するテスト。
	 */
	@Test
	public void testFindACDById() throws Exception {
		
		///// 準備 /////
		
        CD01 cd = new CD01(
        		"CD 1 Title",
        		10.0F,
        		"CD 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");

		CD01 created = itemEJB.createCD(cd);

        ///// テスト /////
        
		// ItemEJBを通じてCD01エンティティを取得
		CD01 found = itemEJB.findCDById(created.getId());
        
        ///// 検証 /////
        
		assertThat(found, is(created));
	}

	/**
	 * 永続化されたCD01エンティティをデータベース上から
	 * 削除するテスト。
	 */
	@Test
	public void testDeleteACD() throws Exception {
		
		///// 準備 /////
		
        CD01 cd = new CD01(
        		"CD 1 Title",
        		10.0F,
        		"CD 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");

		CD01 created = itemEJB.createCD(cd);

        ///// テスト /////
        
		// ItemEJBを通じてCD01エンティティを削除
		itemEJB.deleteCD(created);
        
        ///// 検証 /////
        
		// CD01エンティティが削除されていることを確認
		CD01 found = itemEJB.findCDById(created.getId());

		assertThat(found, is(nullValue()));
	}

	/**
	 * 永続化されたCD01エンティティのフィールド値を更新するテスト。
	 */
	@Test
	public void testUpdateACD() throws Exception {
		
		///// 準備 /////
		
        CD01 cd = new CD01(
        		"CD 1 Title",
        		10.0F,
        		"CD 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");

		CD01 created = itemEJB.createCD(cd);

        ///// テスト /////
        
		// ItemEJBを通じてCD01エンティティを更新
		created.setTitle("CD 1 Title - Updated");
		CD01 updated = itemEJB.updateCD(created);
        
        ///// 検証 /////
        
		assertThat(updated.getTitle(), is("CD 1 Title - Updated"));
		
		// CD01エンティティのフィールド値が更新されていることを確認
		CD01 found = itemEJB.findCDById(created.getId());
		assertThat(found, is(updated));
	}

	/**
	 * 複数のCD01エンティティが永続化されている状態で、
	 * これらすべてのエンティティが取得できることを確認する
	 */
	@Test
	public void testFindAllCDs() throws Exception {
		
		///// 準備 /////
		
        CD01 cd1 = new CD01(
        		"CD 1 Title",
        		10.0F,
        		"CD 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");

        CD01 cd2 = new CD01(
        		"CD 2 Title",
        		20.0F,
        		"CD 2 Description",
        		null,
        		"Music Company 2",
        		2,
        		200.0F,
        		"female");

		CD01 created1 = itemEJB.createCD(cd1);
		CD01 created2 = itemEJB.createCD(cd2);

        ///// テスト /////
        
		// ItemEJBを通じてすべての永続化されたCD01エンティティを取得
		List<CD01> found = itemEJB.findCDs();
        
        ///// 検証 /////
        
		assertThat(found.size(), is(2));
		assertThat(found, hasItems(created1, created2));
	}

}
