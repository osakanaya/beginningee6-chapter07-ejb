package org.beginningee6.book.chapter07.ejb.ex03;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * インスタンスの生成と初期化が連鎖された
 * シングルトン・セッションBeanのテスト。
 * 
 */
@RunWith(Arquillian.class)
public class SingletonChainTest {

	@Deployment
	public static Archive<?> createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class)
				.addPackage(PrimarySingletonEJB.class.getPackage())
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@EJB
	SecondarySingletonEJB secondaryCacheEJB;	// シングルトン・セッションBeanを注入

	/**
	 * SecondarySingletonEJBのインスタンスが初期化されていることを
	 * 確認するテスト。
	 * 
	 * （SecondarySingletonEJBよりも先にPrimarySingletonEJBが生成・初期化
	 * されていることを暗黙的に確認するテストとなっている）
	 */
	@Test
	public void testCacheInitializationForSecondaryCache() throws Exception {
        
        ///// 検証 /////
        
		assertThat(secondaryCacheEJB.getFromCache("KeyA"), is("ItemA"));
		assertThat(secondaryCacheEJB.getFromCache("KeyB"), is("ItemB"));
		assertThat(secondaryCacheEJB.getFromCache("KeyC"), is("ItemC"));
	}
	
	/**
	 * PrimarySingletonEJBのインスタンスが生成・初期化されていることを
	 * 確認するテスト。
	 */
	@Test
	public void testCacheInitializationForPrimaryCache() throws Exception {
        
        ///// 検証 /////
        
		assertThat(secondaryCacheEJB.getFromPrimaryCache(1L), is("ItemA"));
		assertThat(secondaryCacheEJB.getFromPrimaryCache(2L), is("ItemB"));
		assertThat(secondaryCacheEJB.getFromPrimaryCache(3L), is("ItemC"));
	}

}