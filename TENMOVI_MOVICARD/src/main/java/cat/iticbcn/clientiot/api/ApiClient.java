package cat.iticbcn.clientiot.api;

import cat.iticbcn.clientiot.service.TicketService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://54.221.105.50:8000/";
    private static Retrofit retrofit = null;

    public static TicketService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(TicketService.class);
    }
}
