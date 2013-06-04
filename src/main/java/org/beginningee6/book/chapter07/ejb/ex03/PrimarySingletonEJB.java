package org.beginningee6.book.chapter07.ejb.ex03;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * シングルトン・セッションBeanのサンプル。
 * 
 * Beanのインスタンスはコンテナに
 * ただ1つだけ存在し、アプリケーションから
 * このEJBが同時アクセスされる時には常にその１つの
 * インスタンスに対してアクセスが行われる。
 * 
 * ＠Startupアノテーションにより、アプリケーションサーバに
 * このシングルトン・セッションBeanがデプロイされた時に
 * このBeanのインスタンスが生成される。
 * 
 * ＠Startupアノテーションがない場合は、
 * シングルトン・セッションBeanへの最初のアクセスが
 * 発生した時に生成される。
 * 
 * また、シングルトン・セッションBeanのインスタンスが
 * 生成されてからクライアントからのアクセスを受け付ける
 * までの間に任意の初期化処理を実行する場合は、
 * その初期化ロジックを実行したメソッドに＠PostConstruct
 * アノテーションを付加する。
 * 
 */
@Singleton	// シングルトン・セッションBeanであることを宣言する
@Startup	// デプロイ直後にインスタンスを生成するように設定
public class PrimarySingletonEJB {
	
	// キャッシュとして保持するMap
	private Map<Long, String> primaryCache = new HashMap<Long, String>();
	
	/**
	 * ＠PostConstructアノテーションにより、
	 * このメソッドはインスタンスの生成直後に
	 * 自動で実行され、任意の初期化処理が行われる
	 */
	@PostConstruct
	void init() {
		primaryCache.put(1L, "ItemA");
		primaryCache.put(2L, "ItemB");
		primaryCache.put(3L, "ItemC");
	}

	/**
	 * キャッシュであるprimaryCacheに指定したキーのエントリがあれば、
	 * その値を返す
	 * @param id 取得したい値を持つエントリのキー
	 * @return 指定したキーのエントリの値
	 */
	public String getFromCache(Long id) {
		if (primaryCache.containsKey(id)) {
			return primaryCache.get(id);
		} else {
			return null;
		}
	}
}
