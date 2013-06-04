package org.beginningee6.book.chapter07.ejb.ex04;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

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
 * 
 * ローカルインタフェースを介して公開された
 * EJBのメソッドを実行するテスト。
 * 
 * ＠EJBアノテーションでは、ローカルインターフェースで
 * 使用するEJBの注入を行う。
 */
@RunWith(Arquillian.class)
public class ItemEJBLocalInterfaceTest {
	
	private static final Logger logger = Logger.getLogger(ItemEJBLocalInterfaceTest.class
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

	@EJB							// ローカル・インターフェースで
	ItemEJBLocal itemEJBLocal;		// 使用するEJBの注入

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
        
		Book01 returned = itemEJBLocal.createBook(book);
        
        ///// 検証 /////
        
		assertThat(returned.getId(), is(notNullValue()));

		Book01 persisted = em.find(Book01.class, returned.getId());
		assertThat(persisted, is(returned));
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

		Book01 created1 = itemEJBLocal.createBook(book1);
		Book01 created2 = itemEJBLocal.createBook(book2);

        ///// テスト /////
        
		List<Book01> found = itemEJBLocal.findBooks();
        
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
        
		CD01 returned = itemEJBLocal.createCD(cd);
        
        ///// 検証 /////
        
		assertThat(returned.getId(), is(notNullValue()));

		CD01 persisted = em.find(CD01.class, returned.getId());
		assertThat(persisted, is(returned));
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

		CD01 created1 = itemEJBLocal.createCD(cd1);
		CD01 created2 = itemEJBLocal.createCD(cd2);

        ///// テスト /////
        
		List<CD01> found = itemEJBLocal.findCDs();
        
        ///// 検証 /////
        
		assertThat(found.size(), is(2));
		assertThat(found, hasItems(created1, created2));
	}
}
