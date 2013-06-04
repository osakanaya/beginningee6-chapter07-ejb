package org.beginningee6.book.chapter07.ejb.ex07;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.beginningee6.book.chapter07.jpa.ex07.Item07;

/**
 * デプロイメント記述子（ejb-jar.xml）で定義された
 * パラメータの値を環境ネーミング・コンテキストから
 * 取得して処理を行うステートレス・セッションBeanのサンプル。
 * 
 * ejb-jar.xmlでは、<env-entry>タグで環境エントリとして
 * パラメータの名前、値、データ型を記述するようになっている。
 * 
 * ＠Resourceアノテーションを使用すると、ejb-jar.xmlで定義
 * されたパラメータのフィールドの値が注入されるようになる。
 */
@Stateless
public class ItemEJB {

	// 環境ネーミング・コンテキストから、currency に"Euro"が注入される。
	@Resource(name = "currencyEntry")
	private String currency;
	
	// 環境ネーミング・コンテキストから、changeRate に 0.80 が注入される。
	@Resource(name = "changeRateEntry")
	private Float changeRate;
	
	/**
	 * priceをこのクラスのchangeRate倍にして設定。
	 * 
	 * currencyをこのクラスのcurrencyに設定。
	 */
	public Item07 convertPrice(Item07 item) {
		
		item.setPrice(item.getPrice() * changeRate);
		item.setCurrency(currency);
		
		return item;
	}
	
}
