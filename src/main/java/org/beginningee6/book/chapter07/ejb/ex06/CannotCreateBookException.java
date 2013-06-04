package org.beginningee6.book.chapter07.ejb.ex06;

/**
 * 独自に作成したチェック例外。
 * 
 * 以下のアノテーションが例外のクラス宣言に付与されていない場合は、
 * EJBのメソッドから例外をスローしてもトランザクションはロールバックに
 * マークされない。
 * 
 * >> ＠ApplicationException(rollback = true)
 * 
 */
public class CannotCreateBookException extends Exception {

	private static final long serialVersionUID = 1L;

	public CannotCreateBookException() {
		super();
	}

	public CannotCreateBookException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotCreateBookException(String message) {
		super(message);
	}

	public CannotCreateBookException(Throwable cause) {
		super(cause);
	}
	
}
