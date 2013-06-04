package org.beginningee6.book.chapter07.ejb.ex02;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter07.jpa.ex02.Item02;
import org.beginningee6.book.chapter07.jpa.ex02.Sales02;

/**
 * ステートフル・セッションBeanのサンプル。
 * 
 * このタイプのEJBはセッション開始時に
 * 生成され、セッションがタイムアウトするか、
 * ＠Removeアノテーションが付されたメソッドが
 * 呼び出されて破棄されるまで、クライアントに
 * 対して同一のインスタンスが紐付けられる。
 * 
 * このため、このタイプのEJBはBeanのフィールド
 * として保持したデータをメソッド呼び出し間で
 * 参照することができる。
 * （ステートレス・セッションBeanでは、
 * メソッド呼び出しごとに異なるインスタンスが
 * 割り当てられるため、フィールドとして保持した
 * データをメソッド呼び出し間で参照することは
 * できない）
 * 
 * ＠StatefulTimeoutアノテーションを指定すると、
 * ここで指定された時間、EJBのメソッドが実行されない
 * 状態が続くとステートフル・セッションBeanの
 * インスタンスがEJBコンテナによって自動的に
 * 破棄されるようになる
 * 
 */
@Stateful	// ステートフル・セッションBeanであることを宣言
@StatefulTimeout(value = 20, unit = TimeUnit.SECONDS)	// インスタンスが破棄される
														// までのアイドル時間を定義
public class ShoppingCartEJB {
	
	// エンティティのデータアクセス用にEntity Managerを注入
	@PersistenceContext(unitName = "Chapter07ProductionPU")
	private EntityManager em;

	// ステートフル・セッションBeanのインスタンスが内部的に
	// 保持するデータ
	// 
	// ステートフル・セッションBeanのインスタンスが生成されてから
	// のメソッド実行で共通してこのデータを読み書きすることができる
	private List<Item02> cartItems = new ArrayList<Item02>();

	/**
	 * カートにItem02エンティティを追加する
	 * @param item カートに追加するItem02エンティティ
	 */
	public void addItem(Item02	item) {
		// contains()はItem02.equals()に依存
		if (!cartItems.contains(item)) {
			cartItems.add(item);
		}
	}

	/**
	 * カートからItem02エンティティを削除する
	 * @param item カートから削除するItem02エンティティ
	 */
	public void removeItem(Item02 item) {
		// contains()はItem02.equals()に依存
		if (cartItems.contains(item)) {
			cartItems.remove(item);
		}
	}
	
	/**
	 * カートに追加されたItem02エンティティの件数を取得する
	 * @return Item02エンティティの件数
	 */
	public Integer getNumberOfItems() {
		if (cartItems == null || cartItems.isEmpty()) {
			return 0;
		}
		
		return cartItems.size();
	}
	
	/**
	 * カートにあるすべてのItem02エンティティの合計金額を計算する
	 * @return Item02エンティティの合計金額
	 */
	public Float getTotal() {
		if (cartItems == null || cartItems.isEmpty()) {
			return 0f;
		}
		
		Float total = 0f;
		for(Item02 item : cartItems) {
			total += item.getPrice();
		}
		
		return total;
	}

	/**
	 * カートからすべてのItem02エンティティを削除する
	 */
	public void empty() {
		cartItems.clear();
	}
	
	/**
	 * 
	 * カートにある商品（Item02）の売上処理を行う。
	 * 
	 * チェックアウトにより、カートにあるItem02エンティティと
	 * 商品の合計金額と顧客名を元にSales02エンティティが
	 * 永続化される。
	 * 
	 * なお、このメソッドには＠Removeアノテーションが付与されているため、
	 * このメソッド完了後にこのEJBのインスタンスは破棄される。
	 * したがって、このメソッドを実行した後に、同じインスタンスに対して
	 * いずれかの公開メソッドを実行しようとすると例外がスローされる。
	 * 
	 * @param customerName 商品をチェックアウトする顧客の名前
	 * @return 永続化されたSales02エンティティ
	 */
	@Remove
	public Sales02 checkout(String customerName) {
		// Sales02エンティティを生成
		Sales02 sales = new Sales02(customerName, new Date());
		sales.setTotalAmount(getTotal());	// 商品の合計金額
		sales.setItems(cartItems);			// カートにあるItem02エンティティと関連付け
		
		// Item02エンティティを永続化
		for(Item02 item : cartItems) {
			em.persist(item);
		}
		// Sales02エンティティを永続化
		em.persist(sales);
		
		return sales;
	}
}
