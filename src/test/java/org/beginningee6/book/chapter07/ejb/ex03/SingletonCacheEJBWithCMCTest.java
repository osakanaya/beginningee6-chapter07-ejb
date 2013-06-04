package org.beginningee6.book.chapter07.ejb.ex03;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ejb.EJB;

import org.beginningee6.book.chapter07.ejb.ex03.SingletonCacheEJBWithCMC;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * コンテナにより同時実行制御が行われるように実装された
 * シングルトン・セッションBeanのテスト。
 */
@RunWith(Arquillian.class)
public class SingletonCacheEJBWithCMCTest {

	@Deployment
	public static Archive<?> createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class)
				.addPackage(SingletonCacheEJBWithCMC.class.getPackage())
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@EJB
	SingletonCacheEJBWithCMC cacheEJB;		// シングルトン・セッションBeanを注入

	@Before
	public void setUp() throws Exception {
		cacheEJB.clearCache();
	}
	
	/**
	 * ・シングルトン・セッションBeanの持つキャッシュが
	 * 　空の状態で、idを指定してデータを取得する。
	 * ・結果としてnullが取得され、かつ、キャッシュにある
	 * 　エントリの件数は0であることを確認する。
	 */
	@Test
	public void testGetFromCacheWhenCacheIsEmpty() throws Exception {
		
		///// 準備 /////
		
		Object returned = cacheEJB.getFromCache(1L);
        
        ///// 検証 /////
        
		// nullが返却される⇒データは取得できていない
		assertThat(returned, is(nullValue()));
		assertThat(cacheEJB.getNumberOfItems(), is(0));
	}
	
	/**
	 * シングルトン・セッションBeanの持つキャッシュに
	 * 1件のエントリがある状態で、そのエントリのキーを
	 * 指定して値が取得できることを確認する。
	 */
	public void testAddItemAToCacheWhenCacheIsEmpty() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.addToCache(1L, "ItemA");
        
        ///// 検証 /////
        
		String returned = (String)cacheEJB.getFromCache(1L);
		
		// データが取得できている
		assertThat(returned, is("ItemA"));
		assertThat(cacheEJB.getNumberOfItems(), is(1));
	}
	
	/**
	 * シングルトン・セッションBeanの持つキャッシュに
	 * 1件のエントリがある状態で、そのエントリの値が
	 * 書き換えられないことを確認する。
	 * 
	 * （SingletonCacheEJBWithCMCクラスの実装で、
	 * 　idで指定したキーのエントリが既に存在している場合は無視
	 * 　するようになっているため。）
	 */
	@Test
	public void testAddItemBToCacheWhenCacheHasItemAWithSameKey() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.addToCache(1L, "ItemA");

        ///// テスト /////
        
		cacheEJB.addToCache(1L, "ItemB");
        
        ///// 検証 /////
        
		String returned = (String)cacheEJB.getFromCache(1L);
		
		// 最初に設定したデータが取得される
		assertThat(returned, is("ItemA"));
		assertThat(cacheEJB.getNumberOfItems(), is(1));
	}

	/**
	 * ・シングルトン・セッションBeanの持つキャッシュが
	 * 　空の状態で、それぞれキーの異なるエントリをキャッシュに
	 * 　2件追加する。
	 * ・キャッシュにあるエントリの件数が2件であること、
	 * 　追加時に指定したキーでキャッシュから値が取り出せることを
	 * 　確認する。
	 */
	@Test
	public void testAddItemBToCacheWhenCacheHasItemAWithDifferentKey() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.addToCache(1L, "ItemA");

        ///// テスト /////
        
		cacheEJB.addToCache(2L, "ItemB");
        
        ///// 検証 /////
        
		String returnedA = (String)cacheEJB.getFromCache(1L);
		assertThat(returnedA, is("ItemA"));
		String returnedB = (String)cacheEJB.getFromCache(2L);
		assertThat(returnedB, is("ItemB"));

		assertThat(cacheEJB.getNumberOfItems(), is(2));
	}
	
	/**
	 * ・シングルトン・セッションBeanの持つキャッシュが
	 * 　空の状態で、（存在しないキー）のエントリを削除し、
	 * 　エントリの件数に変化が無いことを確認する。
	 * 
	 * （SingletonCacheEJBWithCMCクラスの実装で、
	 * 　指定したキーのエントリが存在しない場合は無視する
	 * 　ようになっているため。）
	 */
	@Test
	public void testRemoveItemFromCacheWhenCacheIsEmpty() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.removeFromCache(1L);
        
        ///// 検証 /////
        
		assertThat(cacheEJB.getNumberOfItems(), is(0));
	}
	
	/**
	 * シングルトン・セッションBeanの持つキャッシュに
	 * 1件のエントリがある状態で、そのエントリのキーを
	 * 指定してエントリが削除できることを確認する。
	 */
	@Test
	public void testRemoveItemFromCacheWhenCacheHasItemWithSameKey() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.addToCache(1L, "ItemA");

        ///// テスト /////
        
		cacheEJB.removeFromCache(1L);
        
        ///// 検証 /////
        
		assertThat(cacheEJB.getNumberOfItems(), is(0));
	}
	
	/**
	 * ・シングルトン・セッションBeanの持つキャッシュに
	 * 　1件のエントリがある状態で、そのエントリは異なるキー
	 * 　を指定してエントリを削除しても、エントリの件数に変化が
	 * 　無いことを確認する。
	 * 
	 * （SingletonCacheEJBWithCMCクラスの実装で、
	 * 　指定したキーのエントリが存在しない場合は無視する
	 * 　ようになっているため。）
	 */
	@Test
	public void testRemoveItemFromCacheWhenCacheHasItemWithDirrefentKey() throws Exception {
		
		///// 準備 /////
		
		cacheEJB.addToCache(1L, "ItemA");

        ///// テスト /////
        
		cacheEJB.removeFromCache(2L);
        
        ///// 検証 /////
        
		String returned1 = (String)cacheEJB.getFromCache(1L);
		assertThat(returned1, is("ItemA"));
		String returned2 = (String)cacheEJB.getFromCache(2L);
		assertThat(returned2, is(nullValue()));

		assertThat(cacheEJB.getNumberOfItems(), is(1));
	}

}