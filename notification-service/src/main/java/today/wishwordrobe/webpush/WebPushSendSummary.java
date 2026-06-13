package today.wishwordrobe.webpush;


public class WebPushSendSummary {
  public int success;
  public int expired;
  public int failed;
  public boolean hasAnySuccess(){
    return success>0;
  }
  public int total(){
    return success+ expired + failed;
  }

  public WebPushSendSummary(int success,int expired,int failed){
    this.success=success;
    this.expired=expired;
    this.failed=failed;
  }
}
