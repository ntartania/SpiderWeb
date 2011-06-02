package spiderweb;

public interface LoadingListener {
	
	public void loadingStarted(int numberLines, String whatIsLoading);
	
	public void loadingChanged(int numberLines, String whatIsLoading);
	
	public void loadingProgress(int lineNumber);
	
	public void loadingComplete();	
}
