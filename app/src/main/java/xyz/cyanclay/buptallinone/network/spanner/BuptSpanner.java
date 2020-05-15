package xyz.cyanclay.buptallinone.network.spanner;

import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.handlers.BoldHandler;

import xyz.cyanclay.buptallinone.network.info.InfoManager;
import xyz.cyanclay.buptallinone.network.spanner.handler.InfoImageHandler;
import xyz.cyanclay.buptallinone.network.spanner.handler.ParagraphHandler;
import xyz.cyanclay.buptallinone.network.spanner.handler.SpanHandler;

public class BuptSpanner extends HtmlSpanner {

    private InfoImageHandler imageHandler;

    public BuptSpanner(InfoManager mgr) {
        super();
        imageHandler = new InfoImageHandler(mgr);
        this.registerHandler("img", imageHandler);
        this.registerHandler("p", new ParagraphHandler(mgr.context));
        this.registerHandler("span", new SpanHandler());
        this.registerHandler("strong", new BoldHandler());
    }

    public void setImageScheme(int maxWidth, boolean force) {
        this.imageHandler.setProps(maxWidth, force);
    }

}
