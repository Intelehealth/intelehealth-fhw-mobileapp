package org.intelehealth.app.ui2.customToolip;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

import me.piruin.quickaction.ActionItem;


public class QuickActionCustom extends PopupWindowsCustom implements PopupWindow.OnDismissListener {

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private static int defaultColor = Color.WHITE;
    private static int defaultTextColor = Color.BLACK;
    private static int defaultDividerColor = Color.argb(32, 0, 0, 0);
    private final int shadowSize;
    private final int shadowColor;
    private boolean enabledDivider;
    private WindowManager windowManager;
    private View rootView;
    private View arrowUp;
    private View arrowDown;
    private LayoutInflater inflater;
    private Resources resource;
    private LinearLayout track;
    private ViewGroup scroller;
    private QuickActionCustom.OnActionItemClickListener mItemClickListener;
    private QuickActionCustom.OnDismissListener dismissListener;
    private List<ActionItemCustom> actionItems = new ArrayList<>();
    private QuickActionCustom.Animation animation = QuickActionCustom.Animation.AUTO;
    private boolean didAction;
    private int orientation;
    private int rootWidth = 0;
    private int dividerColor = defaultDividerColor;
    private int textColor = defaultTextColor;

    /**
     * Constructor for default vertical layout
     *
     * @param context Context
     */
    public QuickActionCustom(@NonNull Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override QuickAction.HORIZONTAL or QuickAction.VERTICAL
     *
     * @param context Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickActionCustom(@NonNull Context context, int orientation) {
        super(context);
        this.orientation = orientation;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        resource = context.getResources();

        shadowSize = resource.getDimensionPixelSize(R.dimen.quick_action_shadow_size);
        shadowColor = resource.getColor(R.color.quick_action_shadow_color);

        setRootView(
                orientation == VERTICAL ? R.layout.quick_action_vertical : R.layout.quick_action_horizontal);
        enabledDivider = orientation == HORIZONTAL;
    }

    private void setRootView(@LayoutRes int id) {
        rootView = inflater.inflate(id, null);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        track = (LinearLayout)rootView.findViewById(R.id.tracks);
        track.setOrientation(orientation);

        arrowDown = rootView.findViewById(R.id.arrow_down);
        arrowUp = rootView.findViewById(R.id.arrow_up);

        scroller = (ViewGroup)rootView.findViewById(R.id.scroller);

        setContentView(rootView);
        setColor(defaultColor);
    }

    /**
     * Set color of QuickAction
     *
     * @param popupColor Color to fill QuickAction
     * @see Color
     */
    public void setColor(@ColorInt int popupColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(popupColor);
        drawable.setStroke(shadowSize, shadowColor);
        drawable.setCornerRadius(resource.getDimension(R.dimen.quick_action_corner));

        arrowDown.setBackground(new ArrowDrawableCustom(ArrowDrawableCustom.ARROW_DOWN, popupColor, shadowSize, shadowColor));
        arrowUp.setBackground(new ArrowDrawableCustom(ArrowDrawableCustom.ARROW_UP, popupColor, shadowSize, shadowColor));
        scroller.setBackground(drawable);
    }

    public static void setDefaultTextColor(int defaultTextColor) {
        QuickActionCustom.defaultTextColor = defaultTextColor;
    }

    public static void setDefaultTextColor(Context context, @ColorRes int defaultTextColor) {
        QuickActionCustom.defaultTextColor = context.getResources().getColor(defaultTextColor);
    }

    public static void setDefaultDividerColor(int defaultDividerColor) {
        QuickActionCustom.defaultDividerColor = defaultDividerColor;
    }

    public static void setDefaultDividerColor(Context context, @ColorRes int defaultDividerColor) {
        QuickActionCustom.defaultDividerColor = context.getResources().getColor(defaultDividerColor);
    }

    public static void setDefaultColor(int defaultColor) {
        QuickActionCustom.defaultColor = defaultColor;
    }

    public static void setDefaultColor(Context context, @ColorRes int setDefaultColor) {
        QuickActionCustom.defaultColor = context.getResources().getColor(setDefaultColor);
    }

    /**
     * Set color of QuickAction by color define in xml resource
     *
     * @param popupColor Color resource id to fill QuickAction
     */
    public void setColorRes(@ColorRes int popupColor) {
        setColor(resource.getColor(popupColor));
    }

    /**
     * Set color for text of each action item. MUST call this before add {@link ActionItem},
     * sorry I'm just too lazy.
     *
     * @param textColorRes Color resource id to use
     */
    public void setTextColorRes(@ColorRes int textColorRes) {
        setTextColor(resource.getColor(textColorRes));
    }

    /**
     * Set color for text of each action item. MUST call this before add {@link ActionItem}, sorry
     * I'm just too lazy.
     *
     * @param textColor Color to use
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    /**
     * Set color for divider. MUST call this before add {@link ActionItem}, sorry I'm just
     * too lazy.
     *
     * @param color use with divider
     */
    public void setDividerColor(int color) {
        this.dividerColor = color;
    }

    /**
     * Set color for text of each action item. MUST call this before add {@link ActionItem}, sorry I'm
     * just too lazy
     *
     * @param colorRes android color resource use with divider
     */
    public void setDividerColorRes(@ColorRes int colorRes) {
        this.dividerColor = resource.getColor(colorRes);
    }

    /**
     * to Enable or Disable divider this must be called before add {@link ActionItem}
     *
     * @param enabled default is true for Horizontal and false Vertical
     */
    public void setEnabledDivider(boolean enabled) {
        this.enabledDivider = enabled;
    }

    /**
     * Set animation style
     *
     * @param mAnimStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(QuickActionCustom.Animation mAnimStyle) {
        this.animation = mAnimStyle;
    }

    /**
     * Set listener for action item clicked.
     *
     * @param listener Listener
     */
    public void setOnActionItemClickListener(QuickActionCustom.OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void addActionItem(final ActionItemCustom... actions) {
        for (ActionItemCustom item : actions) {
            addActionItem(item);
        }
    }

    /**
     * Add action item
     *
     * @param action {@link ActionItem}
     */
    public void addActionItem(final ActionItemCustom action) {
        int position = actionItems.size();
        actionItems.add(action);
        addActionView(position, createViewFrom(action));
    }

    private void addActionView(int position, View actionView) {
        if (enabledDivider && position != 0) {
            position *= 2;
            int separatorPos = position-1;
            View separator = new View(getContext());
            separator.setBackgroundColor(dividerColor);
            int width = resource.getDimensionPixelOffset(R.dimen.quick_action_separator_width);
            ViewGroup.LayoutParams layoutParams = null;
            switch (orientation) {
                case VERTICAL:
                    layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, width);
                    break;
                case HORIZONTAL:
                    layoutParams = new ViewGroup.LayoutParams(width, MATCH_PARENT);
                    break;
            }
            track.addView(separator, separatorPos, layoutParams);
        }
        track.addView(actionView, position);
    }

    @NonNull private View createViewFrom(final ActionItemCustom action) {
        View actionView;
        if (action.haveTitle()) {
            TextView textView = (TextView)inflater.inflate(R.layout.quick_action_item, track, false);
            textView.setTextColor(textColor);
            textView.setText(String.format(" %s ", action.getTitle()));
            if (action.haveIcon()) {
                int iconSize = resource.getDimensionPixelOffset(R.dimen.quick_action_icon_size);
                Drawable icon = action.getIconDrawable(getContext());
                icon.setBounds(0, 0, iconSize, iconSize);
                if (orientation == HORIZONTAL) {
                    textView.setCompoundDrawables(null, icon, null, null);
                } else {
                    textView.setCompoundDrawables(icon, null, null, null);
                }
            }
            actionView = textView;
        } else {
            ImageView imageView =
                    (ImageView)inflater.inflate(R.layout.quick_action_image_item, track, false);
            imageView.setId(action.getActionId());
            imageView.setImageDrawable(action.getIconDrawable(getContext()));
            actionView = imageView;
        }

        actionView.setId(action.getActionId());
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                action.setSelected(true);
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(action);
                }
                if (!action.isSticky()) {
                    didAction = true;
                    dismiss();
                }
            }
        });
        actionView.setFocusable(true);
        actionView.setClickable(true);
        return actionView;
    }

    /**
     * Add action item at specify position
     *
     * @param position to add ActionItem (zero-base)
     * @param action {@link ActionItem}
     */
    public void addActionItem(int position, final ActionItemCustom action) {
        actionItems.add(position, action);
        addActionView(position, createViewFrom(action));
    }

    /**
     * Get action item at an index
     *
     * @param index Index of item (position from callback)
     * @return Action Item at the position
     */
    public ActionItemCustom getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * remove action item
     *
     * @param actionId Id of action to remove
     * @return removed item
     */
    public ActionItemCustom remove(int actionId) {
        return remove(getActionItemById(actionId));
    }

    /**
     * remove action item
     *
     * @param action action to remove
     * @return removed item
     */
    public ActionItemCustom remove(ActionItemCustom action) {
        int index = actionItems.indexOf(action);
        if (index == -1)
            throw new RuntimeException("Not found action");

        if (!enabledDivider) {
            track.removeViewAt(index);
        } else {
            int viewPos = index*2;
            track.removeViewAt(viewPos);
            track.removeViewAt(index == 0 ? 0 : viewPos - 1); //remove divider
        }
        return actionItems.remove(index);
    }

    /**
     * Get action item by Action's ID
     *
     * @param actionId Id of item
     * @return Action Item with same id
     */
    @Nullable public ActionItemCustom getActionItemById(int actionId) {
        for (ActionItemCustom action : actionItems) {
            if (action.getActionId() == actionId)
                return action;
        }
        return null;
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
     *
     * @param activity contain view to be anchor
     * @param anchorId id of view to use as anchor of QuickAction's popup
     */
    public void show(@NonNull Activity activity, @IdRes int anchorId) {
        show(activity.findViewById(anchorId));
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
     *
     * @param anchor view to use as anchor of QuickAction's popup
     */
    public void show(@NonNull View anchor) {
        if (getContext() == null)
            throw new IllegalStateException("Why context is null? It shouldn't be.");

        preShow();

        int xPos, yPos, arrowPos;

        didAction = false;

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0]+anchor.getWidth(),
                location[1]+anchor.getHeight());

        rootView.measure(WRAP_CONTENT, WRAP_CONTENT);

        int rootHeight = rootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = rootView.getMeasuredWidth();
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        int screenHeight = displaymetrics.heightPixels;

        // automatically get X coord of quick_action_vertical (top left)
        if ((anchorRect.left+rootWidth) > screenWidth) {
            xPos = anchorRect.left-(rootWidth-anchor.getWidth());
            xPos = (xPos < 0) ? 0 : xPos;

            arrowPos = anchorRect.centerX()-xPos;
        } else {
            if (anchor.getWidth() > rootWidth) {
                xPos = anchorRect.centerX()-(rootWidth/2);
            } else {
                xPos = anchorRect.left;
            }

            arrowPos = anchorRect.centerX()-xPos;
        }

        int dyTop = anchorRect.top;
        int dyBottom = screenHeight-anchorRect.bottom;

        boolean onTop = dyTop > dyBottom;

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                ViewGroup.LayoutParams l = scroller.getLayoutParams();
                l.height = dyTop-anchor.getHeight();
            } else {
                yPos = anchorRect.top-rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;

            if (rootHeight > dyBottom) {
                ViewGroup.LayoutParams l = scroller.getLayoutParams();
                l.height = dyBottom;
            }
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(@IdRes int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? arrowUp : arrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? arrowDown : arrowUp;

        final int arrowWidth = arrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();

        param.leftMargin = requestedX-arrowWidth/2;

        hideArrow.setVisibility(View.GONE);
    }

    /**
     * Set animation style
     *
     * @param screenWidth screen width
     * @param requestedX distance from left edge
     * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top
     * of anchor view and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX-arrowUp.getMeasuredWidth()/2;
        switch (animation) {
            case AUTO:
                if (arrowPos <= screenWidth/4)
                    mWindow.setAnimationStyle(QuickActionCustom.Animation.GROW_FROM_LEFT.get(onTop));
                else if (arrowPos > screenWidth/4 && arrowPos < 3*(screenWidth/4))
                    mWindow.setAnimationStyle(QuickActionCustom.Animation.GROW_FROM_CENTER.get(onTop));
                else
                    mWindow.setAnimationStyle(QuickActionCustom.Animation.GROW_FROM_RIGHT.get(onTop));
                break;
            default:
                mWindow.setAnimationStyle(animation.get(onTop));
        }
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if the quicakction dialog
     * is dismissed by clicking outside the dialog or clicking on sticky item.
     *
     * @param listener will fire when QuickaAtion dismiss
     */
    public void setOnDismissListener(QuickActionCustom.OnDismissListener listener) {
        setOnDismissListener(this);

        dismissListener = listener;
    }

    @Override public void onDismiss() {
        if (!didAction && dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    public enum Animation {
        GROW_FROM_LEFT {
            @Override int get(boolean onTop) {
                return (onTop) ? R.style.Animation_PopUpMenu_Left : R.style.Animation_PopDownMenu_Left;
            }
        }, GROW_FROM_RIGHT {
            @Override int get(boolean onTop) {
                return (onTop) ? R.style.Animation_PopUpMenu_Right : R.style.Animation_PopDownMenu_Right;
            }
        }, GROW_FROM_CENTER {
            @Override int get(boolean onTop) {
                return (onTop) ? R.style.Animation_PopUpMenu_Center : R.style.Animation_PopDownMenu_Center;
            }
        }, REFLECT {
            @Override int get(boolean onTop) {
                return (onTop) ? R.style.Animation_PopUpMenu_Reflect
                        : R.style.Animation_PopDownMenu_Reflect;
            }
        }, AUTO {
            @Override int get(boolean onTop) {
                throw new UnsupportedOperationException("Can't use this");
            }
        };

        @StyleRes abstract int get(boolean onTop);
    }

    /**
     * Listener for item click
     */
    public interface OnActionItemClickListener {
        void onItemClick(ActionItemCustom item);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {
        void onDismiss();
    }
}
