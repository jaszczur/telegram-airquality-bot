package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCommand implements Command<Message> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCommand.class);
    @Override
    public Flowable<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message msg) {
        logger.warn("Unknown command {}", msg.text());
        return Flowable.never();
    }

    @Override
    public boolean canHandle(Message msg) {
        return true;
    }
}
