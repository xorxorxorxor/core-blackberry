//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Markup.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;
import java.io.IOException;
import java.util.Date;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.evidence.Markup;
import blackberry.evidence.TimestampMarkup;
import blackberry.utils.Utils;


/**
 * The Class UT_Markup.
 */
public final class UT_Markup extends TestUnit {

    /**
     * Instantiates a new u t_ markup.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Markup(final String name, final Tests tests) {
        super(name, tests);
    }

    private void DeleteAllMarkupTest() {
        Markup.removeMarkups();
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {

        DeleteAllMarkupTest();
        SimpleMarkupTest();
        DictMarkupTest();

        return true;
    }

    private void DictMarkupTest() throws AssertException {

        String markupName="TESTTTMARKUP1";
        final TimestampMarkup markup = new TimestampMarkup(markupName);

        AssertThat(markup.isMarkup(), "isMarkup");
        AssertNull(markup.get("FIRST"), "get");

        // aggiunta
        final Date now = new Date();
        markup.put("FIRST", now);
        Date fetch = markup.get("FIRST");
        AssertEqual(now.getTime(), fetch.getTime(), "differents getTime 1");

        // sostituzione
        Date next = new Date(now.getTime() + 1);
        markup.put("FIRST", next);
        fetch = markup.get("FIRST");
        AssertEqual(next.getTime(), fetch.getTime(), "differents getTime 2");

        int maxKey = TimestampMarkup.MAX_DICT_SIZE;
        
        //#ifdef DEBUG
        debug.trace("DictMarkupTest: multi write");
        //#endif
        //multivalue
        for (int i = 0; i < maxKey; i++) {
            next = new Date(now.getTime() + i);
            markup.put("KEY_" + i, next);
        }

        //#ifdef DEBUG
        debug.trace("DictMarkupTest: multi read");
        //#endif
        for (int i = 0; i < maxKey; i++) {
            fetch = markup.get("KEY_" + i);
            AssertNotNull(fetch, "Null Fetch");
            AssertEqual(now.getTime() + i, fetch.getTime(),
                    "differents getTime: " + i);
        }

        // check limits
        markup.put("KEY_ANOTHER", next);
        int nullCount = 0;
        int nullKey=-1;
        for (int i = 0; i < maxKey; i++) {
            fetch = markup.get("KEY_" + i);
            if (fetch == null) {
                nullCount++;
                nullKey=i;
            }
        }
        AssertThat(nullCount == 1, "Not deleted");
        AssertThat(nullKey == 0, "Wrong deleted");

        // check error
        AssertThat(!markup.put(null, next), "put(null,null) should be false");
        AssertThat(!markup.put("Whatever", null),
                "put(null,null) should be false");

        // cancello il markup
        markup.removeMarkup();

        // verifico che sia stato cancellato
        final boolean ret = markup.isMarkup();
        AssertThat(ret == false, "Should Not exist");
    }

    /**
     * Simple markup test.
     * 
     * @throws AssertException
     *             the assert exception
     */
    void SimpleMarkupTest() throws AssertException {


        final Markup markup = new Markup(12345678);

        if (markup.isMarkup()) {
            markup.removeMarkup();
        }

        // senza markup
        boolean ret = markup.isMarkup();
        AssertThat(ret == false, "Should Not exist");

        // scrivo un markup vuoto
        ret = markup.writeMarkup(null);
        AssertThat(ret == true, "cannot write null markup");

        // verifico che ci sia
        ret = markup.isMarkup();
        AssertThat(ret == true, "Should exist");

        // scrivo un numero nel markup
        final byte[] buffer = Utils.intToByteArray(123);

        ret = markup.writeMarkup(buffer);
        AssertThat(ret == true, "cannot write markup");

        // verifico che il numero si legga correttamente
        int value;
        try {
            final byte[] read = markup.readMarkup();
            value = Utils.byteArrayToInt(read, 0);

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.fatal("Markup read");
            //#endif
            throw new AssertException();
        }

        AssertEqual(value, 123, "Wrong read 123");

        // cancello il markup
        markup.removeMarkup();

        // verifico che sia stato cancellato
        ret = markup.isMarkup();
        AssertThat(ret == false, "Should Not exist");

    }

}
