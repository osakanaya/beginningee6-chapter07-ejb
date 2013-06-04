package org.beginningee6.book.chapter07.ejb.ex02;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter07.ejb.ex02.ShoppingCartEJB;
import org.beginningee6.book.chapter07.jpa.ex02.Item02;
import org.beginningee6.book.chapter07.jpa.ex02.Sales02;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ステートフル・セッションBeanのテスト。
 * 
 * ステートレス・セッションBeanと異なり、各テストメソッドの
 * 呼び出しで同じステートフル・セッションBeanのインスタンスが
 * 使用されることによりテストメソッド間の独立性が失われることを
 * 防ぐため、テストメソッド実行の終了ごとに実行されるtearDown()
 * メソッドで＠Removeアノテーションの付いたcheckout()メソッドを
 * 実行し、テストメソッド実行の終了ごとに明示的にインスタンスを
 * 破棄し、次のテストメソッド実行で新規に生成されたインスタンスが
 * 利用されるようにしている。
 * 
 */
@RunWith(Arquillian.class)
public class ShoppingCartEJBTest {
	
	private static final Logger logger = Logger.getLogger(ShoppingCartEJBTest.class
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
				.addPackage(ShoppingCartEJB.class.getPackage())
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
	ShoppingCartEJB cartEJB;	// ステートフル・セッションBeanの注入

	@Before
	public void setUp() throws Exception {
		clearData();
	}
	
	/**
	 * 各テストの後に＠Removeアノテーションが付された
	 * checkout()メソッドを呼び出して、セッションを
	 * 明示的に終了させる。
	 * 
	 * テストメソッド実行の終了後に＠Removeアノテーションの付いた
	 * checkout()メソッドを実行し、明示的にインスタンスが破棄される
	 * ようにしている。
	 * 
	 * これにより、次のテストメソッド実行では新規に生成された
	 * インスタンスが利用されるようになっている。
	 * 
	 */
	@After
	public void tearDown() throws Exception {
		try {
			cartEJB.checkout("Test Customer");
		} catch (Exception e) {
			// checkout()メソッドを実行している
			// テストメソッドでは、テストメソッド終了時点で
			// すでにインスタンスが破棄されているため、
			// ここでのcheckoutメソッドの実行で例外が
			// スローされる。
			// 
			// この例外は無視しても構わない。
		}
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Item02").executeUpdate();
		em.createQuery("DELETE FROM Sales02").executeUpdate();
		userTransaction.commit();
	}

	/**
	 * ステートフル・セッションBeanの初期状態を確認するテスト。
	 * 
	 * カート内の商品の個数と合計金額がともに0であることを
	 * 確認する。
	 */
	@Test
	public void testTotalAndCountWhenCartIsEmpty() throws Exception {
		
        ///// テスト＆検証 /////
		
		// EJBの初期状態の確認
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ・ステートフル・セッションBeanの初期状態から、
	 * 　1つのItem02エンティティをカートに加える。
	 * ・カートにある商品の個数と合計金額が正しく計算される
	 * 　ことを確認する。
	 */
	@Test
	public void testAddAnItemWhenCartIsEmpty() throws Exception {
		
		///// 準備 /////
		
		Item02 item = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");

        ///// テスト /////
        
		cartEJB.addItem(item);
        
        ///// 検証 /////
        
		assertThat(cartEJB.getNumberOfItems(), is(1));
		assertThat(cartEJB.getTotal(), is(23f));
	}
	
	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　1つ追加されている状態で、同じオブジェクトを加える。
	 * ・この時、カートにある商品の個数および合計金額が
	 * 　変化しないことを確認する。
	 * 
	 * （cartEJB.addItemは、List.contains()がfalseの場合のみ
	 * リストに加えるように実装されており、List.contains()は
	 * Item02クラスのequals()メソッドの実装に依存する。
	 * 実装は同じオブジェクト参照または同じ値のオブジェクトで
	 * trueとなるようになっているため、同じオブジェクトを
	 * 加えてもリストにそのオブジェクトが追加されないように
	 * なっている。）
	 * 
	 */
	@Test
	public void testAddItemAWhenCartHasItemA() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);

		int beforeNumItems = cartEJB.getNumberOfItems();
		float beforeTotal = cartEJB.getTotal();
		
        ///// テスト /////
        
		cartEJB.addItem(itemA);
        
        ///// 検証 /////
        
		// 商品の数、合計金額が変化しないことを確認
		assertThat(cartEJB.getNumberOfItems(), 	is(beforeNumItems));
		assertThat(cartEJB.getTotal(), 			is(beforeTotal));
	}

	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　1つ追加されている状態で、追加されているエンティティと
	 * 　同じ値を持つ別のオブジェクトを加える。
	 * ・この時、カートにある商品の個数および合計金額が
	 * 　変化しないことを確認する。
	 * 
	 * （cartEJB.addItemは、List.contains()がfalseの場合のみ
	 * リストに加えるように実装されており、List.contains()は
	 * Item02クラスのequals()メソッドの実装に依存する。
	 * 実装は同じオブジェクト参照または同じ値のオブジェクトで
	 * trueとなるようになっているため、同じフィールド値を持つ
	 * オブジェクトを加えてもリストにそのオブジェクトが追加
	 * されないようになっている。）
	 */
	@Test
	public void testAddItemAWhenCartHasItemBofSameValue() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);

		int beforeNumItems = cartEJB.getNumberOfItems();
		float beforeTotal = cartEJB.getTotal();

		///// テスト /////
        
		Item02 itemB = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemB);
        
        ///// 検証 /////
        
		// 商品の数、合計金額が変化しないことを確認
		assertThat(cartEJB.getNumberOfItems(), 	is(beforeNumItems));
		assertThat(cartEJB.getTotal(), 			is(beforeTotal));
	}

	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　1つ追加されている状態で、追加されているエンティティと
	 * 　異なる値を持つ別のオブジェクトを加える。
	 * ・カートにある商品の個数と合計金額が追加したエンティティ
	 * 　の分だけ増加することを確認する。
	 */
	@Test
	public void testAddItemBWhenCartHasItemA() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		Item02 itemB = new Item02("The Hitchhiker's Guide to the Galaxy", 12.5F, "Science fiction comedy book");
		cartEJB.addItem(itemA);

		int beforeNumItems = cartEJB.getNumberOfItems();
		float beforeTotal = cartEJB.getTotal();

		///// テスト /////
        
		cartEJB.addItem(itemB);
        
        ///// 検証 /////
        
		// 商品の数、合計金額が追加したエンティティの分だけ増加することを確認
		assertThat(cartEJB.getNumberOfItems(), 	is(beforeNumItems + 1));
		assertThat(cartEJB.getTotal(), 			is(beforeTotal + 12.5f));
	}
	
	/**
	 * ・ステートフル・セッションBeanの初期状態（カートに商品がない状態）から、
	 * 　Item02エンティティを削除する。
	 * ・カートの商品の個数と合計金額が0のまま変化しないことを確認する。
	 */
	@Test
	public void testRemoveAnItemWhenCartIsEmpty() throws Exception {
		
		///// 準備 /////
		
		Item02 item = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");

        ///// テスト /////
        
		// メソッド内部で無視される
		cartEJB.removeItem(item);
        
        ///// 検証 /////
        
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ステートフル・セッションBeanのカートにItem02エンティティが
	 * 追加されている状態で、同じオブジェクトを削除する。
	 * 
	 * （cartEJB.removeItemは、List.contains()がtrueの場合のみ
	 * リストから削除するように実装されており、List.contains()は
	 * Item02クラスのequals()メソッドの実装に依存する。
	 * 実装は同じオブジェクト参照または同じ値のオブジェクトで
	 * trueとなるようになっているため、同じオブジェクトを
	 * 引数に指定したばあいは、そのオブジェクトがリストから
	 * 削除されるようになっている。）
	 * 
	 */
	@Test
	public void testRemoveItemAWhenCartHasItemA() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);

        ///// テスト /////
        
		cartEJB.removeItem(itemA);
        
        ///// 検証 /////
        
		// 削除されている
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ステートフル・セッションBeanのカートにItem02エンティティが
	 * 加えられている状態で、同じ値を持つ別のオブジェクトを削除する。
	 * 
	 * （cartEJB.removeItemは、List.contains()がtrueの場合のみ
	 * リストから削除するように実装されており、List.contains()は
	 * Item02クラスのequals()メソッドの実装に依存する。
	 * 実装は同じオブジェクト参照または同じ値のオブジェクトで
	 * trueとなるようになっているため、同じ値を持つ別のオブジェクトを
	 * 引数に指定した場合は、同じ値を持つオブジェクトがリストから
	 * 削除されるようになっている。）
	 */
	@Test
	public void testRemoveItemAWhenCartHasItemAofSameValue() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);

        ///// テスト /////
        
		Item02 itemB = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.removeItem(itemB);
        
        ///// 検証 /////
        
		// 削除されている
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　加えられている状態で、カートに無いItem02エンティティを
	 * 　引数に指定してこれをカートから削除しようとする。
	 * ・カートにある商品の個数や合計金額が変化しないことを
	 * 　確認する。
	 * 
	 */
	@Test
	public void testRemoveItemBWhenCartHasItemA() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		Item02 itemB = new Item02("The Hitchhiker's Guide to the Galaxy", 12.5F, "Science fiction comedy book");
		cartEJB.addItem(itemA);
		
		int beforeNumItems = cartEJB.getNumberOfItems();
		float beforeTotal = cartEJB.getTotal();

        ///// テスト /////
        
		// メソッド内部で無視される
		cartEJB.removeItem(itemB);
        
        ///// 検証 /////
        
		// 個数と合計金額に変化はない
		assertThat(cartEJB.getNumberOfItems(), is(beforeNumItems));
		assertThat(cartEJB.getTotal(), is(beforeTotal));
	}
	
	/**
	 * ・ステートフル・セッションBeanの初期状態（カートに商品が無い状態）から、
	 * 　カートをクリアする。
	 * ・カートにある商品の個数、合計金額が0であることを確認する。
	 */
	@Test
	public void testRemoveAllItemsWhenCartIsEmpty() throws Exception {

        ///// テスト /////
        
		cartEJB.empty();
        
        ///// 検証 /////
        
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　加えられている状態で、itemリストをクリアする。
	 * ・カートにある商品の個数、合計金額が0であることを確認する。
	 */
	@Test
	public void testRemoveAllItemsWhenCartHasItemA() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);

        ///// テスト /////
        
		cartEJB.empty();
        
        ///// 検証 /////
        
		// itemリストは空になっている
		assertThat(cartEJB.getNumberOfItems(), is(0));
		assertThat(cartEJB.getTotal(), is(0f));
	}
	
	/**
	 * ・ステートフル・セッションBeanのカートにItem02エンティティが
	 * 　2つある状態でチェックアウトする。
	 * ・カートにある2つのItem02エンティティおよびこれらの合計金額を
	 * 　フィールドに持つSales02エンティティが永続化されることを
	 * 　確認する。
	 * 
	 * （cartEJB.checkout()メソッドには＠Removeアノテーションが
	 * 付されており、このメソッドの完了後、EJBのインスタンスが破棄され、
	 * セッションが終了する。）
	 */
	@Test
	public void testCheckoutWhenCartHasItemAAndItemB() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		Item02 itemB = new Item02("The Hitchhiker's Guide to the Galaxy", 12.5F, "Science fiction comedy book");
		cartEJB.addItem(itemA);
		cartEJB.addItem(itemB);

        ///// テスト /////
        
		Sales02 returned = cartEJB.checkout("Test Customer");
		// ここでセッション終了
        
        ///// 検証 /////
        
		// データを確認
		assertThat(returned.getCustomerName(), 	is("Test Customer"));
		assertThat(returned.getTotalAmount(), 	is(35.5f));
		assertThat(returned.getItems().size(), 	is(2));
		assertThat(returned.getItems(), 		hasItems(itemA, itemB));

		// Entity Managerを使用して、エンティティを
		// データベースから取得してデータが永続化されていることを確認
		Sales02 persisted = em.find(Sales02.class, returned.getId());

		// Sales02エンティティが永続化されていることを確認
		assertThat(persisted.getCustomerName(), is("Test Customer"));
		assertThat(persisted.getTotalAmount(), 	is(35.5f));
		assertThat(persisted.getItems().size(), is(2));
		// 2つのItem02エンティティが永続化されていることを確認
		assertThat(persisted.getItems(), 		hasItems(itemA, itemB));
		
	}
	
	/**
	 * ・チェックアウトした後、再度チェックアウトする。
	 * ・1回目のチェックアウトで＠Removeアノテーションによりステートフル・
	 * 　セッションBeanのインスタンスは破棄されるので、2回目の
	 * 　チェックアウト時にはこのインスタンスがないことになり、
	 * 　javax.ejb.NoSuchEJBExceptionがスローされることを確認する。
	 * 
	 */
	@Test
	public void testCheckoutWhenCartAlreadyCheckedOut() throws Exception {
		
		///// 準備 /////
		
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		Item02 itemB = new Item02("The Hitchhiker's Guide to the Galaxy", 12.5F, "Science fiction comedy book");
		cartEJB.addItem(itemA);
		cartEJB.addItem(itemB);
		
		// チェックアウトによりステートフル・セッションBeanの
		// インスタンスは破棄される
		cartEJB.checkout("Test Customer");

        ///// テスト /////
        
		try {
			cartEJB.checkout("Test Customer");
			// 例外が発生する
			fail("Should throw exception");
		} catch (Exception e) {
			// 例外はNoSuchEJBException
			assertThat(e, is(instanceOf(javax.ejb.NoSuchEJBException.class)));
		}
		
	}
	
	/**
	 * ・ステートフル・セッションBeanのインスタンス生成、または、
	 * 　最後にステートフル・セッションBeanのメソッドを実行してから
	 * 　一定時間（ここでは＠StatefulTimeoutアノテーションにより
	 * 　20秒に設定）アイドル状態（メソッドを実行しない状態）であると
	 * 　タイムアウトすると、インスタンスが破棄されることを確認する。
	 */
	@Test
	public void testEJBRemovedBeforeCheckout() throws Exception {

        ///// テスト /////
        
		Item02 itemA = new Item02("Zoot Allure", 23f, "Another Zappa's master piece");
		cartEJB.addItem(itemA);		// アイドル前の最後のEJB実行
		
		try {
			TimeUnit.SECONDS.sleep(25);	// 余裕を持って20秒以上待機
		} catch (Exception e) {
		}
		
		// この時点では、セッションがタイムアウトし、
		// ステートフル・セッションBeanのインスタンスは破棄されている
		
		Item02 itemB = new Item02("The Hitchhiker's Guide to the Galaxy", 12.5F, "Science fiction comedy book");

		try {
			// メソッドを実行しようとしてもインスタンスは破棄されているため、
			// 例外がスローされる。
			cartEJB.addItem(itemB);
			cartEJB.checkout("Test Customer");
			fail("Should throw exception");
		} catch (Exception e) {
			// 例外はNoSuchEJBException
			assertThat(e, is(instanceOf(javax.ejb.NoSuchEJBException.class)));
		}
        
        ///// 検証 /////
        
		TypedQuery<Sales02> query = em.createNamedQuery("Sales02.findAllSales", Sales02.class);
		List<Sales02> persisted = query.getResultList();
		
		assertThat(persisted.size(), is(0));

	}

}
