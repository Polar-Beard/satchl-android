package me.atheneum.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import me.atheneum.R;
import me.atheneum.lists.CustomStyles;
import me.atheneum.model.Story;
import me.atheneum.requests.AuthJsonObjectRequest;
import me.atheneum.util.CustomHtml;

public class WritingActivity extends AppCompatActivity {

    private final static int TITLE_CHAR_LIMIT = 75;
    private final static int DESCRIPTION_CHAR_LIMIT = 150;
    private static final String URL_PUBLISH = "http://104.236.163.131:9000/api/story/publish";
    private static final String PREFS_NAME = "CredentialPrefsFile";

    private static boolean lastCharIsEnter = false;

    private static boolean titleIsActive;
    private static boolean subtitleIsActive;
    private static boolean boldIsActive;
    private static boolean italicIsActive;
    private static boolean boldItalicIsActive;
    private static boolean formattedListIsActive;
    private static boolean unformattedListIsActive;

    private static Toolbar toolbarBottom;
    private EditText bodyTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);

        toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
        if(toolbarBottom == null){
            return;
        }
        bodyTextInput = (EditText) findViewById(R.id.editText);
        bodyTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_NULL &&
                        event.getAction() == KeyEvent.ACTION_DOWN){
                            System.out.println("Enter key pressed");
                }
                return true;
            }
        });
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int cursorPos = bodyTextInput.getSelectionStart();
                SpannableStringBuilder bodyText  = new SpannableStringBuilder(bodyTextInput.getText());
                int[] lineBoundaries = getLineBoundaries(cursorPos, bodyTextInput);
                int[] wordBoundaries = getWordBoundaries(cursorPos, bodyTextInput);
                SpannableStringBuilder currentLine = new SpannableStringBuilder(bodyText.subSequence(lineBoundaries[0],lineBoundaries[1]));
                SpannableStringBuilder currentWord = new SpannableStringBuilder(bodyText.subSequence(wordBoundaries[0], wordBoundaries[1]));
                switch (item.getItemId()) {
                    case R.id.action_format_size:
                        if(titleIsActive){
                            currentLine = removeTitleSpan(currentLine);
                            currentLine = addSubtitleSpan(currentLine);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentLine,lineBoundaries));
                            setTitleToInactive();
                            setSubtitleToActive();
                        } else if(subtitleIsActive){
                            currentLine = removeTitleSpan(currentLine);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentLine,lineBoundaries));
                            setSubtitleToInactive();
                        } else{
                            bodyText.replace(lineBoundaries[0], lineBoundaries[1], addTitleSpan(currentLine));
                            bodyTextInput.setText(bodyText);
                            setTitleToActive();
                        }
                        bodyTextInput.setSelection(cursorPos);
                        break;
                    case R.id.action_bold:
                        if(boldItalicIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            currentWord = addItalicSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentWord,wordBoundaries));
                            setBoldItalicToInactive();
                            setBoldToInactive();
                        } else if(boldIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText, currentWord, wordBoundaries));
                            setBoldToInactive();
                        } else if(italicIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            currentWord = addBoldItalicSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentWord,wordBoundaries));
                            setBoldToActive();
                            setBoldItalicToActive();
                        } else{
                            bodyText.replace(wordBoundaries[0], wordBoundaries[1], addBoldSpan(currentWord));
                            bodyTextInput.setText(bodyText);
                            setBoldToActive();
                        }
                        bodyTextInput.setSelection(cursorPos);
                        break;
                    case R.id.action_italic:
                        if(boldItalicIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            currentWord = addBoldSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentWord,wordBoundaries));
                            setBoldItalicToInactive();
                            setItalicToInactive();
                        } else if(italicIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText, currentWord, wordBoundaries));
                            setItalicToInactive();
                        } else if(boldIsActive){
                            currentWord = removeStyleSpan(currentWord);
                            currentWord = addBoldItalicSpan(currentWord);
                            bodyTextInput.setText(updateTextSpan(bodyText,currentWord,wordBoundaries));
                            setItalicToActive();
                            setBoldItalicToActive();
                        } else{
                            bodyText.replace(wordBoundaries[0], wordBoundaries[1], addItalicSpan(currentWord));
                            bodyTextInput.setText(bodyText);
                            setItalicToActive();
                        }
                        bodyTextInput.setSelection(cursorPos);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        toolbarBottom.inflateMenu(R.menu.activity_writing_toolbar_bottom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_writing_toolbar_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        RequestQueue queue = Volley.newRequestQueue(this);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_publish) {
            //Get first line to use as title
            int[] titleBounds = getLineBoundaries(0,bodyTextInput);
            String title = bodyTextInput.getText().toString().substring(0,titleBounds[1]);
            if(title.length() >= TITLE_CHAR_LIMIT){
                title = title.substring(0,TITLE_CHAR_LIMIT);
            }
            //Get second line to use as description
            int[] descriptionBounds = getLineBoundaries(titleBounds[1] + 1, bodyTextInput);
            String description = bodyTextInput.getText().toString().substring(titleBounds[1] + 1, descriptionBounds[1]);
            if(description.length()>= DESCRIPTION_CHAR_LIMIT){
                description = description.substring(0,DESCRIPTION_CHAR_LIMIT);
            }
            String body = CustomHtml.toHtml(bodyTextInput.getText());
            Story story = new Story();
            story.setTitle(title);
            story.setDescription(description);
            story.setBody(body);
            Gson gson = new Gson();
            String postBody = gson.toJson(story);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(postBody);
            } catch (Exception e){
                return true;
            }
            AuthJsonObjectRequest jsonObjectRequest = new AuthJsonObjectRequest(URL_PUBLISH,jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("It worked!");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("Posting didn't work!");
                }
            }
            );
            SharedPreferences credentials = getSharedPreferences(PREFS_NAME, 0);
            String emailAddress = credentials.getString("emailAddress", null);
            String password = credentials.getString("password", null);
            if(emailAddress == null|| password == null){
                System.out.println("Credentials do not exist");
                return true;
            } else{
                jsonObjectRequest.setEmailAddress(emailAddress);
                jsonObjectRequest.setPassword(password);
                queue.add(jsonObjectRequest);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private static SpannableString getWord(int cursorPos, EditText editText){
        int [] wordBoundaries = getWordBoundaries(cursorPos, editText);
        return new SpannableString(editText.getText().subSequence(wordBoundaries[0], wordBoundaries[1]));
    }

    private static int[] getWordBoundaries(int cursorPos, EditText editText) {
        return getStringBoundsByCharLimit(cursorPos,editText,' ');
    }
    private static SpannableString getLine(int cursorPos, EditText editText){
        int[] lineBoundaries = getLineBoundaries(cursorPos, editText);
        return new SpannableString(editText.getText().subSequence(lineBoundaries[0],lineBoundaries[1]));
    }
    private static int[] getLineBoundaries(int cursorPos, EditText editText) {
        return getStringBoundsByCharLimit(cursorPos,editText,'\n');
    }

    private static int[] getStringBoundsByCharLimit(int cursorPos,EditText editText,char boundary){
        CharSequence enteredText = editText.getText();
        //Find nearest boundary that precedes the cursor.
        int stringStartPos = 0;
        boolean foundStartingBoundary = false;
        int i = cursorPos - 1;
        while(!foundStartingBoundary && i > 0 ){
            if(enteredText.charAt(i) == boundary){
                stringStartPos = i;
                foundStartingBoundary = true;
            }
            i--;
        }
        //Find the next nearest boundary.
        int stringEndPos = enteredText.length();
        if(cursorPos != enteredText.length()) {
            boolean foundEndingBoundary = false;
            int j = cursorPos;
            while (!foundEndingBoundary && j < enteredText.length()) {
                if (enteredText.charAt(j) == boundary) {
                    stringEndPos = j;
                    foundEndingBoundary = true;
                }
                j++;
            }
        }
        return new int[] {stringStartPos,stringEndPos};
    }

    private SpannableStringBuilder addTitleSpan(SpannableStringBuilder text){
        text.setSpan(new RelativeSizeSpan(CustomStyles.TITLE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private SpannableStringBuilder removeTitleSpan(SpannableStringBuilder text){
        RelativeSizeSpan[] spans = text.getSpans(0,text.length(),RelativeSizeSpan.class);
        if(spans.length != 0) {
            text.removeSpan(spans[0]);
        }
        return text;
    }

    private SpannableStringBuilder addSubtitleSpan(SpannableStringBuilder text){
        text.setSpan(new RelativeSizeSpan(CustomStyles.SUBTITLE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private SpannableStringBuilder addItalicSpan(SpannableStringBuilder text){
        text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private SpannableStringBuilder addBoldSpan(SpannableStringBuilder text){
        text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private SpannableStringBuilder addBoldItalicSpan(SpannableStringBuilder text){
        text.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private SpannableStringBuilder removeStyleSpan(SpannableStringBuilder text){
        StyleSpan[] spans = text.getSpans(0,text.length(),StyleSpan.class);
        if(spans.length != 0){
            text.removeSpan(spans[0]);
        }
        return text;
    }

    private SpannableStringBuilder updateTextSpan(SpannableStringBuilder originalText, SpannableStringBuilder newText, int[] boundaries){
        SpannableStringBuilder updatedText = new SpannableStringBuilder();
        /* This gets a bit hacky, but I couldn't figure out a better solution.
                                Instead of using replace(), the content from the EditText is
                                reconstructed around the current line, from which the
                                old span has been removed. The reason being that replace()
                                was preserving the old span, and I couldn't figure out how to
                                remove it.
                             */
        if(boundaries[0]== 0){
            updatedText.append(newText);
        } else{
            CharSequence textBeforeCurrentLine = originalText.subSequence(0,boundaries[0]);
            updatedText.append(textBeforeCurrentLine);
            updatedText.append(newText);
        }
        if(boundaries[1]!= originalText.length()){
            CharSequence textAfterCurrentLine = originalText.subSequence(boundaries[1], originalText.length());
            updatedText.append(textAfterCurrentLine);
        }
        return updatedText;
    }

    private static void setBoldToActive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_bold);
        item.setIcon(R.drawable.ic_bold_active);
        boldIsActive = true;
        if(italicIsActive){
            setBoldItalicToActive();
        }
    }

    private static void setItalicToActive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_italic);
        item.setIcon(R.drawable.ic_italic_active);
        italicIsActive = true;
        if(boldIsActive){
            setBoldItalicToActive();
        }
    }

    private static void setBoldItalicToActive(){
        boldItalicIsActive = true;
    }

    private static void setTitleToActive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_format_size);
        item.setIcon(R.drawable.ic_title_active);
        subtitleIsActive = false;
        titleIsActive = true;
    }

    private static void setSubtitleToActive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_format_size);
        item.setIcon(R.drawable.ic_subtitle_active);
        titleIsActive = false;
        subtitleIsActive = true;
    }

    private static void setUnformattedListToActive(){
        unformattedListIsActive = true;
    }

    private static void setFormattedListToActive(){
        formattedListIsActive = true;
    }

    private static void setBoldToInactive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_bold);
        item.setIcon(R.drawable.ic_bold_inactive);
        boldIsActive = false;
        if(boldItalicIsActive){
            setBoldItalicToInactive();
        }
    }

    private static void setItalicToInactive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_italic);
        item.setIcon(R.drawable.ic_italic_inactive);
        italicIsActive = false;
        if(boldItalicIsActive){
            setBoldItalicToInactive();
        }
    }

    private static void setBoldItalicToInactive(){
        boldItalicIsActive = false;
    }

    private static void setTitleToInactive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_format_size);
        item.setIcon(R.drawable.ic_title_inactive);
        titleIsActive = false;
    }

    private static void setSubtitleToInactive(){
        MenuItem item = toolbarBottom.getMenu().findItem(R.id.action_format_size);
        item.setIcon(R.drawable.ic_title_inactive);
        subtitleIsActive = false;
    }

    private static void setUnformattedListToInactive(){
        unformattedListIsActive = false;
    }

    private static void setFormattedListToInactive(){
        formattedListIsActive = false;
    }

    private static void setAllToInactive(){
        setBoldToInactive();
        setItalicToInactive();
        setBoldItalicToInactive();
        setTitleToInactive();
        setSubtitleToInactive();
        setFormattedListToInactive();
        setUnformattedListToInactive();
    }


    public static class EditTextCursorWatcher extends EditText {

        private int lastCursorPos;

        public EditTextCursorWatcher(Context context, AttributeSet attrs,
                                     int defStyle) {
            super(context, attrs, defStyle);
        }

        public EditTextCursorWatcher(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public EditTextCursorWatcher(Context context) {
            super(context);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            boolean textChanged = false;
            SpannableString currentLine = getLine(selStart, this);
            SpannableString currentWord = getWord(selStart, this);
            if(selStart == selEnd && selStart != lastCursorPos && selStart != 0) {
                if(selStart != lastCursorPos + 1) {
                    if (getText() != null) {
                        StyleSpan[] wordSpans = currentWord.getSpans(0, currentWord.length(), StyleSpan.class);
                        RelativeSizeSpan[] lineSpans = currentLine.getSpans(0, currentLine.length(), RelativeSizeSpan.class);
                        if(wordSpans.length > 0){
                            for(StyleSpan span: wordSpans){
                                switch (span.getStyle()) {
                                    case Typeface.BOLD:
                                        setBoldToActive();
                                        break;
                                    case Typeface.ITALIC:
                                        setItalicToActive();
                                        break;
                                    case Typeface.BOLD_ITALIC:
                                        setBoldItalicToActive();
                                        break;
                                }
                            }
                        }
                        if (lineSpans.length > 0) {
                            for (RelativeSizeSpan span : lineSpans) {
                                float style = span.getSizeChange();
                                if (style == CustomStyles.TITLE) {
                                    setTitleToActive();
                                } else if (style == CustomStyles.SUBTITLE) {
                                    setSubtitleToActive();
                                }
                            }
                        }
                        if(wordSpans.length == 0 && lineSpans.length == 0){
                            setAllToInactive();
                        }
                    }
                }

                //Check to see if Enter is pressed, used to end active formatting states
                char lastChar = this.getText().charAt(selStart - 1);
                if(lastChar == '\n'){
                    setAllToInactive();
                }
                else {
                    if (boldItalicIsActive) {
                       currentLine.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, currentLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textChanged = true;
                    } else if (italicIsActive) {
                        currentLine.setSpan(new StyleSpan(Typeface.ITALIC), 0, currentLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textChanged = true;
                    } else if (boldIsActive) {
                        currentLine.setSpan(new StyleSpan(Typeface.BOLD), 0, currentLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textChanged = true;
                    }
                    if (titleIsActive) {
                        currentLine.setSpan(new RelativeSizeSpan(CustomStyles.TITLE), 0, currentLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textChanged = true;
                    }
                    if (subtitleIsActive) {
                        currentLine.setSpan(new RelativeSizeSpan(CustomStyles.SUBTITLE), 0, currentLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textChanged = true;
                    }
                }
            }
            lastCursorPos = selStart;
            if(textChanged) {
                int[] bounds = getLineBoundaries(selStart, this);
                CharSequence text = this.getText().replace(bounds[0], bounds[1], currentLine);
                this.setText(text);
                this.setSelection(selStart);
            }
        }
    }

}
