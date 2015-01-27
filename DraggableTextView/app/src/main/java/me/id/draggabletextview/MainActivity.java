package me.id.draggabletextview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements View.OnTouchListener, View.OnDragListener
{


    //region ArrayList
    private List<String> lstrectangleLeft = new ArrayList<String>();
    private List<String> lstrectangleTop = new ArrayList<String>();
    private List<String> lstrectangleRight = new ArrayList<String>();
    private List<String> lstrectangleBottom = new ArrayList<String>();
    private List<Rect> lstRectangles = new ArrayList<Rect>();

    //endregion

    //region Variables
    private String inputCount;
    WebView webView;
    WebViewClient webViewClient;
    //endregion


    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetListenersDraggableCoupon();
        BindWebView();
    }


    //region Setters

    /**
     * Sets this Class as the Ontouch and on Drag Listener
     */
    private void SetListenersDraggableCoupon()
    {
        TextView coupon = (TextView) findViewById(R.id.txtlabel);
        coupon.setOnTouchListener(this);
        coupon.setOnDragListener(this);
        findViewById(R.id.llStuff).setOnDragListener(this);
    }

    /**
     * Allows back button to navigate back in the Web View History or close when it's reached the last page.
     */
    @Override
    public void onBackPressed()
    {
        if (webView.canGoBack())
        {
            webView.goBack();
        }
        else
        {
            finish();
        }
    }


    //endregion

    //region Binders

    /**
     * Sets up the Web View with the custom client and configured everything.
     */
    private void BindWebView()
    {

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new IdmeJavaScriptInterface(this), "android");
        webView.loadUrl("http://www.google.com");

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);


        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(final WebView view, String url)
            {
                super.onPageFinished(view, url);
                view.loadUrl(
                        "javascript:android.setInputCount(document.getElementsByTagName('input').length);");


            }
        });

    }


    //region Utilities

    /**
     * Copies any String to the Clipboard.
     * @param textToCopy text to be put into clipboard.
     */
    private void CopyToClipBoard(String textToCopy)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, textToCopy);
        clipboard.setPrimaryClip(clip);
    }


    //endregion


    //region DraggableCouponLogic


    //region RectangleLogic

    /**
     * Clears the Rectangles Array List
     */
    private void ClearRectangleList()
    {
        lstRectangles.clear();
        lstrectangleTop.clear();
        lstrectangleLeft.clear();
    }

    /**
     * Create Rectangles that based on the number of inputs found on the Site
     */
    private void CreateRectangles()
    {
        try
        {
            for (int i = 0; i < Integer.parseInt(inputCount); i++)
            {
                Rect myRectangle = new Rect(Math.round(Float.parseFloat(lstrectangleLeft.get(i))),
                        Math.round(Float.parseFloat(lstrectangleTop.get(i))),
                        Math.round(Float.parseFloat(lstrectangleRight.get(i))),
                        Math.round(Float.parseFloat(lstrectangleBottom.get(i))));
                lstRectangles.add(myRectangle);
            }
        } catch (IndexOutOfBoundsException e)
        {
            Log.e("Index Out Of Bounds Exception : ", e.getMessage());
        } catch (NumberFormatException i)
        {
            Log.e("Number Format Exception : " ,i.getMessage());
        }

    }
    //endregion


    //region EventListener
    //This is the Drag Listener.

    /**
     * The On Drag Listener, response to events based on it's context.
     * @param layoutview the layout of the activity.
     * @param dragEvent the drag event
     */
    public boolean onDrag(View layoutview, DragEvent dragEvent)
    {
        int action = dragEvent.getAction();
        switch (action)
        {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                //We create the rectangles to Maintain Everything in one place
                CreateRectangles();
                CoreCodeLogic(dragEvent);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                // We use this to prevent the Coupon from getting lost if dragged above the Action Bar.
                View view1 = (View) dragEvent.getLocalState();
                if (dragEvent.getX() == 0.0 || dragEvent.getY() == 0.0)
                {
                    view1.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
        return true;
    }

    //This is what Executes the drag.

    /**
     * The on touch event
     * @param view the view of the activity
     * @param motionEvent the motion even to be evaluated.
     * @return true or false depending on what the Movement Even is.
     */
    public boolean onTouch(View view, MotionEvent motionEvent)
    {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            //Copy to Clipboard Just In case Drag and Drop doesn't Work
            TextView txtCoupon = (TextView) findViewById(R.id.txtlabel);

            CopyToClipBoard(txtCoupon.getText().toString());
            //We find all input fields here instead of the scrolling.
            FindAllInputFields();
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
        else
        {
            return false;
        }


    }
    //endregion

    //region Animation

    /**
     * Animates View
     * @param view  View to be animated
     * @param xStart The X Axis Start Point (Original Point)
     * @param yStart The Y Axis Start point (Original Point)
     */
    private void Animation(View view, float xStart, float yStart)
    {
        Animation animation = new TranslateAnimation(xStart - (view.getX() / 9), 0,
                yStart - (view.getY() / 3), 0);
        animation.setDuration(500);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }
    //endregion

    //region CoreCodeLogic

    /**
     * Core Logic, Evaluates Dragevent and inserts the text in the Input Field
     * @param dragEvent
     */
    private void CoreCodeLogic(DragEvent dragEvent)
    {
        View view = (View) dragEvent.getLocalState();

        //Get display Metrics so we can divide the x and y by the density and X and Y are on the same scale as the webview

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float xLocation = dragEvent.getX() / metrics.density;
        float yLocation = dragEvent.getY() / metrics.density;

        TextView coupon = (TextView) findViewById(R.id.txtlabel);
        int height = (int) Math.round(coupon.getHeight() / (metrics.density * 1.5));

        //Subtract From Y Location because it needs it to properly calculate everything, otherwise Y Locations do not match.
        int subtractValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height,
                getResources().getDisplayMetrics());

        yLocation -= subtractValue;

        float xTrans = dragEvent.getX() - view.getX();
        float yTrans = dragEvent.getY() - view.getY();

        CheckLocation(xLocation, yLocation);
        Animation(view, xTrans, yTrans);
    }
    //endregion

    //region CheckLocation

    /**
     * Verifies location to input if x and y match the drop.
     * @param xLocation X Location of Dropped Event
     * @param yLocation Y Location of Dropped Event
     */
    private void CheckLocation(float xLocation, float yLocation)
    {
        for (int i = 0; i < lstRectangles.size(); i++)
        {
            Rect inputFrame = lstRectangles.get(i);
            boolean xCondition = (xLocation >= inputFrame.left && xLocation <= inputFrame.right);
            boolean yCondition = (yLocation >= inputFrame.top && yLocation <= inputFrame.bottom);

            if (xCondition && yCondition)
            {
                TextView couponTest = (TextView) findViewById(R.id.txtlabel);
                //Inject Code with Void so Chrome Web Kit doesn't assume it's a URL
                webView.loadUrl(
                        "javascript:void(document.getElementsByTagName('input')[" + String.valueOf(
                                i) + "].value = '" + couponTest.getText() + "');");
            }
        }

    }
    //endregion

    //region FindInputFields

    /**
     * Finds All Input fields and Puts them into Rect Values
     */
    private void FindAllInputFields()
    {
        ClearRectangleList();
        try
        {
            for (int i = 0; i < Integer.parseInt(inputCount); i++)
            {
                String d = String.valueOf(i);
                String InjectedJavaScript = "";
                InjectedJavaScript += "javascript:android.setRectangleLeft(document.getElementsByTagName('input')[" + d + "].getBoundingClientRect().left);";
                InjectedJavaScript += "android.setRectangleTop(document.getElementsByTagName('input')[" + d + "].getBoundingClientRect().top);";
                InjectedJavaScript += "android.setRectangleRight(document.getElementsByTagName('input')[" + d + "].getBoundingClientRect().right);";
                InjectedJavaScript += "android.setRectangleBottom(document.getElementsByTagName('input')[" + d + "].getBoundingClientRect().bottom);";
                webView.loadUrl(InjectedJavaScript);

            }
        } catch (NumberFormatException e)
        {
           Log.e("Number Format Exception: " , e.getMessage());

        }
    }
    //endregion
    //endregion

    /**
     * JavaScript Interface Class, Code to be executed within Javascript in Web View.
     */
    private class IdmeJavaScriptInterface
    {
        Context mContext;


        //This is the JavaScript interface, These methods are called within the webview.

        IdmeJavaScriptInterface(Context c)
        {
            mContext = c;
        }

        @JavascriptInterface
        public void setInputCount(String count)
        {
            inputCount = count;
        }

        @JavascriptInterface
        public void setRectangleLeft(String value)
        {
            lstrectangleLeft.add(value);
        }

        @JavascriptInterface
        public void setRectangleRight(String value)
        {
            lstrectangleRight.add(value);
        }

        @JavascriptInterface
        public void setRectangleBottom(String value)
        {
            lstrectangleBottom.add(value);
        }

        @JavascriptInterface
        public void setRectangleTop(String value)
        {
            lstrectangleTop.add(value);
        }
    }

}
