package com.mohamedfadiga.nubchat.pubnubcallbacks;

import com.fasterxml.jackson.databind.JsonNode;
import com.mohamedfadiga.nubchat.services.BackgroundService;
import com.mohamedfadiga.nubchat.utils.DatabaseHelper;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.History;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import java.util.List;

public class HistoryCallback extends PNCallback<PNHistoryResult>
{
    private History history;
    private BackgroundService service;
    private String username, channelName;

    public HistoryCallback(History history, BackgroundService service, String username, String channelName)
    {
        this.history = history;
        this.service = service;
        this.username = username;
        this.channelName = channelName;
    }

    @Override
    public void onResponse(PNHistoryResult result, PNStatus status)
    {
        if (!status.isError())
        {
            DatabaseHelper db = DatabaseHelper.getInstance(service);
            List<PNHistoryItemResult> messages = result.getMessages();
            for (PNHistoryItemResult iR:messages)
            {
                try {
                    JsonNode n = iR.getEntry();
                    String sender = n.get("sender").asText();
                    if (sender.equals(username)) continue;
                    service.newMessage(db.saveMessage(n,channelName.equals(username)?sender:channelName, iR.getTimetoken()));
                }
                catch (Exception e){e.printStackTrace();}
            }
            if(messages.size() == 100)history.start(result.getEndTimetoken()+1).async(this);
        }
    }
}