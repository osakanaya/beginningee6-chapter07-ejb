package org.beginningee6.book.chapter07.ejb.ex07;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter07.jpa.ex07.Item07;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 環境ネーミング・コンテクストのテスト。
 */
@RunWith(Arquillian.class)
public class ItemEJBTest {

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
				// このファイル内に環境エントリが定義されている
				.addAsWebInfResource("ejb-jar.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction userTransaction;

	@EJB
	ItemEJB itemEJB;			// ステートレスセッションBeanの注入

	/**
	 * 環境ネーミング・コンテキストで設定された通過単位、価格の倍率で
	 * Item07エンティティが更新されることを確認する。
	 */
	@Test
	public void testEnvironmentNamingContext() throws Exception {
		
		///// 準備 /////
		
        Item07 item = new Item07();
        item.setTitle("The Hitchhiker's Guide to the Galaxy");
        item.setPrice(12.5F);
        item.setIsbn("1-84023-742-2");
        item.setNbOfPage(354);
        item.setIllustrations(false);

        ///// テスト /////
        
        Item07 returned = itemEJB.convertPrice(item);
        
        ///// 検証 /////
        
        // priceは0.80倍になっている
        assertThat(returned.getPrice(), is(12.5F * 0.8F));
        // currencyは"Euros"になっている
        assertThat(returned.getCurrency(), is("Euros"));
	}
}
