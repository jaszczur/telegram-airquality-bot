package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.response.BaseResponse;

public class MessageDeliveryException extends RuntimeException {
    private BaseResponse msg;

    public MessageDeliveryException(BaseResponse msg) {
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return "" + msg.errorCode() + ": " +  msg.description();
    }
}
