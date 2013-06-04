package org.beginningee6.book.chapter07.ejb.ex03;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * シングルトン・セッションBeanのサンプル。
 * 
 * ＠DependOnアノテーションにより、シングルトン・
 * セッションBeanのインスタンス生成・初期化の
 * 順序を規定することができる。
 * 
 * ここの例では、PrimarySingletonEJBがSecondarySingletonEJBよりも
 * 先に生成・初期化されるように指定されている。
 * 
 * クラスのフィールドに＠EJBアノテーションを付与することによって、
 * 別のEJB（この例ではPrimarySingletonEJB）を注入することができる。
 * 
 * 
 */
@Singleton	// シングルトン・セッションBeanであることを宣言する
@Startup	// デプロイ直後にインスタンスを生成するように設定
@DependsOn("PrimarySingletonEJB")	// PrimarySingletonEJBの生成後に
									// このBeanのインスタンスを生成
public class SecondarySingletonEJB {
	
	// キャッシュとして保持するMap
	private Map<String, String> secondaryCache = new HashMap<String, String>();

	@EJB			// 別のシングルトン・セッションBeanを注入
	private PrimarySingletonEJB primaryCacheEJB;
	
	/**
	 * ＠PostConstructアノテーションにより、
	 * このメソッドはインスタンスの生成直後に
	 * 自動で実行され、任意の初期化処理が行われる
	 */
	@PostConstruct
	void init() {
		// SecondarySingletonEJBが持つキャッシュの初期化を行う。
		// このキャッシュが持つエントリの値には、PrimarySingletonEJB
		// が持つキャッシュの値を使う必要がある。
		// このため、＠DependsOnアノテーションを使用して、
		// このEJBのキャッシュが依存するPrimarySingletonEJBを先に
		// 初期化するようにしている。
		secondaryCache.put("KeyA", primaryCacheEJB.getFromCache(1L));
		secondaryCache.put("KeyB", primaryCacheEJB.getFromCache(2L));
		secondaryCache.put("KeyC", primaryCacheEJB.getFromCache(3L));
	}
	
	/**
	 * キャッシュであるsecondaryCacheに指定したキーのエントリがあれば、
	 * その値を返す
	 * @param id 取得したい値を持つエントリのキー
	 * @return 指定したキーのエントリの値
	 */
	public String getFromCache(String key) {
		if (secondaryCache.containsKey(key)) {
			return secondaryCache.get(key);
		} else {
			return null;
		}
	}

	/**
	 * PrimarySingletonEJBがもつキャッシュにおいて、指定したキーの
	 * エントリがあれば、その値を返す
	 * 
	 * テスト用途として、SecondarySingletonEJBから直接PrimarySingletonEJBの
	 * キャッシュの内容にアクセスするメソッドとして位置付けられる。
	 * @param id 取得したい値を持つエントリのキー
	 * @return 指定したキーのエントリの値
	 */
	public String getFromPrimaryCache(Long id) {
		return primaryCacheEJB.getFromCache(id);
	}
}
