package org.beginningee6.book.chapter07.ejb.ex03;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * コンテナにより同時実行制御が行われるように実装された
 * シングルトン・セッションBeanのサンプル。
 * 
 * ＠ConcurrencyManagementアノテーションにより、
 * シングルトン・セッションBeanの各メソッドを
 * 実行したときのインスタンスの同期を
 * コンテナ管理とするか、Bean管理とするかを
 * 選択することができる。
 * 
 * この例では、ConcurrencyManagementType.CONTAINER
 * によりインスタンスの同期をコンテナ管理するように
 * 設定している。（＠ConcurrencyManagementアノテーション
 * を省略した場合は、コンテナ管理となる。
 * 
 * ＠Lockアノテーションを付与することにより、メソッド実行時の
 * 同期の種類を指定することができる。（指定なしの場合は、
 * LockType.WRITEとなる）
 * 
 * 同期の種類には、以下のものがある。
 * 
 * １．LockType.READ（共有ロック）
 * 　・他のクライアントがLockType.READのメソッドを実行中であっても、
 * 　　並行して同じもしくは別のLockType.READのメソッドを実行する
 * 　　ことができる。
 * 
 * ２．LockType.WRITE（排他ロック）
 * 　・LockType.WRITEのメソッドを実行している時は、この実行が終わるまで
 * 　　他のクライアントによるLockType.READやLockType.WRITEのメソッドの
 * 　　実行がブロックされる。
 * 
 * ＠Lockアノテーションをクラス宣言に付与することで、
 * シングルトンBeanのすべてのメソッドに対して一律に
 * 同期の種類を指定することができる。
 * 
 * また、メソッド宣言ごとに＠LockTypeアノテーションを付与することにより、
 * クラス宣言で設定した同期の種類の指定を上書きすることができる。
 * 
 * ＠AccessTimeoutアノテーションにより、ブロックのタイムアウト時間を指定できる。
 * valueを-１にすると無限に待機し、０にするとメソッドの並列実行が許可されなくなる。
 * タイムアウトするとCuncurrentAccessExceptionが発生する。
 * 
 */
@Singleton	// シングルトン・セッションBeanであることを宣言する
@Startup	// デプロイ直後にインスタンスを生成するように設定
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)	// コンテナ管理の同時実行制御
@Lock(LockType.READ)	// すべてのメソッドの同期タイプとしてLockType.READを設定
public class SingletonCacheEJBWithCMC {
	
	// キャッシュとして保持するMap
	private Map<Long, Object> cache = new HashMap<Long, Object>();
	
	/**
	 * キャッシュにエントリを追加する
	 * 
	 * 同期化されないHashMapの更新を伴うので、排他ロックにより
	 * HashMap更新の同期を図る。
	 * 
	 * @param id 追加するエントリのキー
	 * @param object 追加するエントリの値
	 */
	@AccessTimeout(value = 20, unit = TimeUnit.SECONDS)	// ブロックのタイムアウトを20秒に設定
	@Lock(LockType.WRITE)								// 同期タイプをLockType.WRITEでオーバライド
	public void addToCache(Long id, Object object) {
		if (!cache.containsKey(id)) {
			cache.put(id, object);
		}
	}
	
	/**
	 * キャッシュからエントリを削除する
	 * @param id 削除するエントリのキー
	 * 
	 * 同期化されないHashMapの更新を伴うので、排他ロックにより
	 * HashMap更新の同期を図る。
	 * 
	 */
	@AccessTimeout(value = 20, unit = TimeUnit.SECONDS)	// ブロックのタイムアウトを20秒に設定
	@Lock(LockType.WRITE)								// 同期タイプをLockType.WRITEでオーバライド
	public void removeFromCache(Long id) {
		if (cache.containsKey(id)) {
			cache.remove(id);
		}
	}
	
	/**
	 * キャッシュにあるすべてのエントリを削除する
	 * 
	 * 同期化されないHashMapの更新を伴うので、排他ロックにより
	 * HashMap更新の同期を図る。
	 * 
	 */
	@AccessTimeout(value = 20, unit = TimeUnit.SECONDS)	// ブロックのタイムアウトを20秒に設定
	@Lock(LockType.WRITE)								// 同期タイプをLockType.WRITEでオーバライド
	public void clearCache() {
		cache.clear();
	}
	
	/**
	 * 指定したキーのエントリが持つ値を取得する
	 * 
	 * クラスに指定されているLockType.READで同期する
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
