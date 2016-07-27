package vnkim.util;


public class VNKIMException extends Exception
{

    public VNKIMException(Throwable throwable)
    {
        super(throwable);
    }

    public VNKIMException(String s)
    {
        super(s);
    }

    public VNKIMException()
    {
    }
}
