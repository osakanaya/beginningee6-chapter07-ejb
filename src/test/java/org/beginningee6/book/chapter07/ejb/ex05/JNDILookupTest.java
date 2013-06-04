package org.beginningee6.book.chapter07.ejb.ex05;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
 * ＠EJBアノテーションを利用した、EJBコンテナによる自動的なEJBの注入を行わずに、
 * JNDIルックアップによりEJBを取得して、そのメソッドを実行するテスト。
 * 
 * 以下の2つの軸で表現されるパターンの組み合わせでテストメソッドを構成している。
 * 
 * ・JNDIルックアップにより取得するEJBの種類
 * 　（ローカルインタフェースで使用するEJB、リモートインタフェースで使用するEJB、
 * 　　インタフェースなしで使用するEJB）
 * ・JNDIルックアップの対象となる名前空間のスコープ
 * 　（グローバル、アプリケーション、モジュール）
 */
@RunWith(Arquillian.class)
public class JNDILookupTest {
	
	private static final Logger logger = Logger.getLogger(JNDILookupTest.class
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
				// Webアーカイブ名を"test-module.war"として作成
				.create(WebArchive.class, "test-module.war")
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

	@Before
	public void setUp() throws Exception {
		clearData();
		loadData();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Book01").executeUpdate();
		
		userTransaction.commit();
	}
	
	/**
	 * 各テストの開始前にエンティティを１つデータベースへ登録しておく
	 */
	private void loadData() throws Exception {
		Book01 book = new Book01(
				"Book 1 Title", 
				10.0F, 
				"Book 1 Description", 
				"1-11111-111-1", 
				111, 
				true);
		
		// JNDIルックアップを使用してEJBを取得
		InitialContext ctx = new InitialContext();
        ItemEJB itemEJB = (ItemEJB) ctx.lookup("java:global/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJB");

        // エンティティを永続化
        itemEJB.createBook(book);
	}

	/**
	 * グローバルスコープで、インタフェースなしで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:global[/<app-name>]/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * <app-name>　 ：earファイルの拡張子を取り除いた名前（セッションBeanがearに
	 * 　　　　　　　 パッケージされている場合のみに必要）
	 * <module-name>：EJBがパッケージングされるアーカイブから拡張子を取り除いた名前
	 * 　　　　　　　（このテストでは、createDeployment()メソッドでtest-module.warという
	 * 　　　　　　　　アーカイブでパッケージングしているため、test-moduleを指定する）
	 * <bean-name>　：セッションBeanの名前（通常はBeanクラスの名前と同じ）
	 * <fully-qualified-interface-name>
	 * 　　　　　　 ：EJBにアクセスするインタフェースのクラス名（FQDN形式）
	 * 　　　　　　　 インタフェースなしで使用するEJBを取得するので、
	 * 　　　　　　　 Beanクラスのクラス名を記述する
	 */
	@Test
	public void testJNDILookupNoInterfaceViewFromGlobaScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// "java:global/test-module/ItemEJB!...ItemEJB" でルックアップ
        ItemEJB itemEJB = (ItemEJB) ctx.lookup("java:global/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJB");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * アプリケーションスコープで、インタフェースなしで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:app/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<module-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupNoInterfaceViewFromApplicationScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// "java:app/test-module/ItemEJB!...ItemEJB" でルックアップ
        ItemEJB itemEJB = (ItemEJB) ctx.lookup("java:app/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJB");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * モジュールスコープで、インタフェースなしで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:module/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<bean-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupNoInterfaceViewFromModuleScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// "java:module/ItemEJB!...ItemEJB" でルックアップ
        ItemEJB itemEJB = (ItemEJB) ctx.lookup("java:module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJB");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * グローバルスコープで、ローカルインタフェースで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:global[/<app-name>]/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * <app-name>　 ：earファイルの拡張子を取り除いた名前（セッションBeanがearに
	 * 　　　　　　　 パッケージされている場合のみに必要）
	 * <module-name>：EJBがパッケージングされるアーカイブから拡張子を取り除いた名前
	 * 　　　　　　　（このテストでは、createDeployment()メソッドでtest-module.warという
	 * 　　　　　　　　アーカイブでパッケージングしているため、test-moduleを指定する）
	 * <bean-name>　：セッションBeanの名前（通常はBeanクラスの名前と同じ）
	 * <fully-qualified-interface-name>
	 * 　　　　　　 ：EJBにアクセスするインタフェースのクラス名（FQDN形式）
	 * 　　　　　　　 ローカルインタフェースで使用するEJBを取得するので、
	 * 　　　　　　　 ローカルインタフェースのクラス名を記述する
	 */
	@Test
	public void testJNDILookupLocalInterfaceFromGlobaScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// ローカル・インターフェース
		// "java:global/test-module/ItemEJB!...ItemEJBLocal" でルックアップ
        ItemEJBLocal itemEJB = (ItemEJBLocal) ctx.lookup("java:global/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBLocal");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * アプリケーションスコープで、ローカルインタフェースで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:app/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<module-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupLocalInterfaceFromApplicationScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// ローカル・インターフェース
		// "java:app/test-module/ItemEJB!...ItemEJBLocal" でルックアップ
        ItemEJBLocal itemEJB = (ItemEJBLocal) ctx.lookup("java:app/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBLocal");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * モジュールスコープで、ローカルインタフェースでで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:module/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<bean-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupLocalInterfaceFromModuleScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// ローカル・インターフェース
		// "java:module/ItemEJB!...ItemEJBLocal" でルックアップ
        ItemEJBLocal itemEJB = (ItemEJBLocal) ctx.lookup("java:module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBLocal");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * グローバルスコープで、リモートインタフェースで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:global[/<app-name>]/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * <app-name>　 ：earファイルの拡張子を取り除いた名前（セッションBeanがearに
	 * 　　　　　　　 パッケージされている場合のみに必要）
	 * <module-name>：EJBがパッケージングされるアーカイブから拡張子を取り除いた名前
	 * 　　　　　　　（このテストでは、createDeployment()メソッドでtest-module.warという
	 * 　　　　　　　　アーカイブでパッケージングしているため、test-moduleを指定する）
	 * <bean-name>　：セッションBeanの名前（通常はBeanクラスの名前と同じ）
	 * <fully-qualified-interface-name>
	 * 　　　　　　 ：EJBにアクセスするインタフェースのクラス名（FQDN形式）
	 * 　　　　　　　 リモートインタフェースで使用するEJBを取得するので、
	 * 　　　　　　　 リモートインタフェースのクラス名を記述する
	 */
	@Test
	public void testJNDILookupRemoteInterfaceFromGlobaScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// リモート・インターフェース
		// "java:global/test-module/ItemEJB!...ItemEJBRemote" でルックアップ
        ItemEJBRemote itemEJB = (ItemEJBRemote) ctx.lookup("java:global/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBRemote");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * アプリケーションスコープで、リモートインタフェースで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:app/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<module-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupRemoteInterfaceFromApplicationScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// リモート・インターフェース
		// "java:app/test-module/ItemEJB!...ItemEJBRemote" でルックアップ
        ItemEJBRemote itemEJB = (ItemEJBRemote) ctx.lookup("java:app/test-module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBRemote");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

	/**
	 * モジュールスコープで、リモートインタフェースで使用するEJBを取得する。
	 * 
	 * この時に指定するJNDI名の形式は、以下の通り。
	 * 
	 * java:module/<bean-name>[!<fully-qualified-interface-name>]
	 * 
	 * ※<bean-name>以降に設定する値は、グローバルスコープでの説明を参照のこと。
	 * 
	 */
	@Test
	public void testJNDILookupRemoteInterfaceFromModuleScope() throws Exception {
		
		///// 準備 /////
		
		InitialContext ctx = new InitialContext();

        ///// テスト /////
        
		// リモート・インターフェース
		// "java:module/ItemEJB!...ItemEJBRemote" でルックアップ
        ItemEJBRemote itemEJB = (ItemEJBRemote) ctx.lookup("java:module/ItemEJB!org.beginningee6.book.chapter07.ejb.ex05.ItemEJBRemote");
        List<Book01> found = itemEJB.findBooks();
        
        ///// 検証 /////
        
        assertThat(found.size(), is(1));
	}

}
