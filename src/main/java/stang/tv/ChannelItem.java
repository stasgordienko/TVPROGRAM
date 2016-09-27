package stang.tv;

import java.util.Date;

/**
 * Created by Administrator on 25.09.2016.
 */
public class ChannelItem {

    String id;
    String name;
    String tvURL;
    String category;

    ChannelItem(String _id, String _name, String _tvURL, String _category) {
        id = _id;
        name = _name;
        tvURL = _tvURL;
        category = _category;
    }
}
