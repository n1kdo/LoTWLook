package com.n1kdo.adif;

public class AdifResultException extends Exception
{
    private static final long serialVersionUID = 1L;

    public static final int INVALID_CREDENTIALS_EXCEPTION = 0;
    public static final int LOGIN_FAILED_EXCEPTION = 1;
    public static final int IO_EXCEPTION = 2;
    public static final int INVALID_ADIF_RESULT = 3;

    private static final String messages[] = { "Username/Password Invalid", "Login Failed", "Network Read Error",
            "Invalid ADIF Result" };

    private final int exceptionType;
    private final String exceptionText;

    public AdifResultException(int exceptionType) {
        this.exceptionType = exceptionType;
        this.exceptionText = messages[exceptionType];
    }

    public AdifResultException(int exceptionType, Exception e) {
        super(e);
        this.exceptionType = exceptionType;
        this.exceptionText = messages[exceptionType];
    }

    public AdifResultException(int exceptionType, String exceptionText) {
        this.exceptionType = exceptionType;
        this.exceptionText = exceptionText;
    }

    public final int getExceptionType()
    {
        return exceptionType;
    }

    public final String getMessage()
    {
        return exceptionText;
    }
}
