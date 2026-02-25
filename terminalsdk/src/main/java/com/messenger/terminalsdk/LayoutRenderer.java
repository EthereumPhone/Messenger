package com.messenger.terminalsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.PorterDuff;
import android.widget.ImageView;

public class LayoutRenderer {

    private Context context;

    public LayoutRenderer(Context context) {
        this.context = context;
    }

    /**
     * Renders the send terminal layout with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderSend() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.send_terminal_layout, null);

        // Apply accent color to all interactive elements (icons + labels)
        int accentColor = getColorForRender();

        // Re-tint icons
        ImageView sendIcon = view.findViewById(R.id.send_icon);
        if (sendIcon != null) {
            sendIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView sendLabel = view.findViewById(R.id.send_request_label);
        if (sendLabel != null) {
            sendLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the send request terminal layout with custom text into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderSendRequest() {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.send_request_terminal_layout, null);

        // Apply accent color to all interactive elements (icons + labels)
        int accentColor = getColorForRender();

        // Re-tint icons
        ImageView sendIcon = view.findViewById(R.id.send_icon);
        if (sendIcon != null) {
            sendIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        // Re-color labels
        TextView sendLabel = view.findViewById(R.id.send_request_label);
        if (sendLabel != null) {
            sendLabel.setTextColor(accentColor);
        }

        // Measure and layout the view with exact dimensions (428x142 pixels)
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        // Create a bitmap with the exact dimensions
        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);

        // Create a canvas to draw the view onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the next terminal layout into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderNext() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.next_terminal_layout, null);

        int accentColor = getColorForRender();

        ImageView nextIcon = view.findViewById(R.id.next_icon);
        if (nextIcon != null) {
            nextIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        TextView nextLabel = view.findViewById(R.id.next_label);
        if (nextLabel != null) {
            nextLabel.setTextColor(accentColor);
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the confirm terminal layout into a bitmap
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderConfirm() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirm_terminal_layout, null);

        int accentColor = getColorForRender();

        ImageView confirmIcon = view.findViewById(R.id.delete_icon);
        if (confirmIcon != null) {
            confirmIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        TextView confirmLabel = view.findViewById(R.id.delete_label);
        if (confirmLabel != null) {
            confirmLabel.setTextColor(accentColor);
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Renders the add-member terminal layout into a bitmap.
     * @return A bitmap of size 428x142 pixels containing the rendered layout
     */
    public Bitmap renderAddMember() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.add_member_terminal_layout, null);

        int accentColor = getColorForRender();

        ImageView addIcon = view.findViewById(R.id.add_member_icon);
        if (addIcon != null) {
            addIcon.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }

        TextView addLabel = view.findViewById(R.id.add_member_label);
        if (addLabel != null) {
            addLabel.setTextColor(accentColor);
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(428, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(142, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, 428, 142);

        Bitmap bitmap = Bitmap.createBitmap(428, 142, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Iterate over all pixels and set any pixel that is not close to black to the provided color.
     * @param bmp         The bitmap to manipulate.
     * @param toColor     The color to apply to non-black pixels.
     * @param tolerance   Maximum value (0-255) that R, G, and B can have while still being considered black.
     */
    private void replaceNonBlack(Bitmap bmp, int toColor, int tolerance) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];
            int r = Color.red(px);
            int g = Color.green(px);
            int b = Color.blue(px);

            // If any channel is above the tolerance, treat as non-black
            if (r > tolerance || g > tolerance || b > tolerance) {
                pixels[i] = toColor;
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * Renders a black layout with the given text in RED and centered on the bitmap.
     * The rendered bitmap size is 428x142 px – identical to the mini-display FrameLayout.
     *
     * @param text The text to be rendered on the layout.
     * @return A bitmap containing the rendered layout.
     */
    public Bitmap renderBlackText(String text) {
        // Inflate the black layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.black_layout, null);

        // Try to set the text on the TextView inside the layout (expected id: messageText)
        TextView tv = view.findViewById(R.id.messageText);
        if (tv != null) {
            tv.setText(text);
            Typeface typeface = ResourcesCompat.getFont(context, R.font.monomaniac);
            tv.setTypeface(typeface);
            tv.setTextColor(getColorForRender());
            tv.setGravity(Gravity.CENTER);
        }

        // Measure & layout the view (fixed 428 × 142 px)
        int width = 428;
        int height = 142;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, width, height);

        // Create bitmap and draw the view onto it
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private int getColorForRender() {
        int accentColor = Settings.Secure.getInt(
                context.getContentResolver(),
                "systemui_accent_color",
                0xFFFE0000  // Default red
        );

        switch (accentColor){
            //TERMINAL
//            case -13510400:
//                primaryColor = terminalCore
//                secondaryColor = terminalHack
//            break;
            //LAZER
            case -131072:
                return 0xFFFF0000;
            //OCEAN
//            case -16718593:
//                primaryColor = oceanCore
//                secondaryColor = oceanAbyss
//            break;
            //ORCHE
//            case -1012183:
//                primaryColor = orcheCore
//                secondaryColor = orcheAsh
//            break;
            //GUNMETAL
//            case -3618616:
//                primaryColor = gunMetalCore
//                secondaryColor = gunMetalForge
//            break;
        }

        return accentColor;
    }
}