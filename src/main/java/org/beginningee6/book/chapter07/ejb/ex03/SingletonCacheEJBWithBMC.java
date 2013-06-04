package org.beginningee6.book.chapter07.ejb.ex03;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Beanでの実装により同時実行制御を行うように実装された
 * シングルトン・セッションBeanのサンプル。
 * 
 * ＠ConcurrencyManagementアノテーションに
 * ConcurrencyManagementType.BEANを設定することにより、
 * Beanでの実装により同時実行制御を行うことが宣言される。
 * 
 * Bean管理の同時実行制御では、シングルトンBeanに対する
 * 同時アクセスがあった時の内部データの同期に関しては、
 * EJBコンテナは何も関与せず、同期に関する責務はBeanの
 * 実装者にゆだねられる。
 * 
 * Beanの実装では、メソッドをsynchronizedキーワードにより
 * 修飾するなどして同時実行制御を実装する。
 */
@Singleton	// シングルトン・セッションBeanであることを宣言する
@Startup	// デプロイ直後にインスタンスを生成するように設定
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)	// Bean管理の同時実行制御
public class SingletonCacheEJBWithBMC {
	
	// キャッシュとして保持するMap
	private Map<Long, Object> cache = new HashMap<Long, Object>();
	
	/**
	 * キャッシュにエントリを追加する
	 * 
	 * 同期化されないHashMapの更新を伴うので、synchronizedキーワード
	 * をメソッド宣言に追加してメソッド実行時にインスタンスをロック
	 * することによりHashMap更新の同期を図る。
	 * 
	 * @param id 追加するエントリのキー
	 * @param object 追加するエントリの値
	 */
	public synchronized void addToCache(Long id, Object object) {
		if (!cache.containsKey(id)) {
			cache.put(id, object);
		}
	}
	
	/**
	 * キャッシュからエントリを削除する
	 * @param id 削除するエントリのキー
	 * 
	 * 同期化されないHashMapの更新を伴うので、synchronizedキーワード
	 * をメソッド宣言に追加してメソッド実行時にインスタンスをロック
	 * することによりHashMap更新の同期を図る。
	 * 
	 */
	public synchronized void removeFromCache(Long id) {
		if (cache.containsKey(id)) {
			cache.remove(id);
		}
	}
	
	/**
	 * キャッシュにあるすべてのエントリを削除する
	 * 
	 * 同期化されないHashMapの更新を伴うので、synchronizedキーワード
	 * をメソッド宣言に追加してメソッド実行時にインスタンスをロック
	 * することによりHashMap更新の同期を図る。
	 * 
	 */
	public synchronized void clearcCache() {
		cache.clear();
	}
	
	/**
	 * 指定したキーのエントリが持つ値を取得する
	 * 
	 * @param id 取得したい値を持つエントリのキー
	 * @return 指定したキーのエントリが持つ値
	 */
	public Object getFromCache(Long id) {
		if (cache.containsKey(id)) {
			return cache.get(id);
		} else {
			return null;
		}
	}
	
	/**
	 * キャッシュにあるエントリの数を取得する
	 * 
	 * クラスに指定されているLockType.READで同期する
	 * 
	 * @return キャッシュにあるエントリの数
	 */
	public Integer getNumberOfItems() {
		if (cache == null || cache.size() == 0) {
			return 0;
		}
		
		return cache.size();
	}
}
