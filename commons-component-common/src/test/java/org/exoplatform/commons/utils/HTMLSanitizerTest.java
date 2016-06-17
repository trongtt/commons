/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.utils;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.junit.Test;
import org.owasp.html.examples.EbayPolicyExample;

/**
 * Created by kmenzli on 16/06/16.
 */
public class HTMLSanitizerTest extends BaseCommonsTestCase {

    public void testEmpty() throws Exception {
        assertEquals("", HTMLSanitizer.sanitize(""));
        assertEquals("", HTMLSanitizer.sanitize(null));
    }

    public void testEncodeImg() throws Exception {
        String input1 = "<img alt='crying' height='23' src='http://localhost:8080/CommonsResources/ckeditor/plugins/smiley/images/cry_smile.png' title='crying' width='23' onerror='alert('XSS')' onmousemove='alert('XSS1')'/>";
        assertEquals("<img alt=\"crying\" height=\"23\" src=\"http://localhost:8080/CommonsResources/ckeditor/plugins/smiley/images/cry_smile.png\" title=\"crying\" width=\"23\" />", HTMLSanitizer.sanitize(input1));
    }

    public void testSanitizeRemovesScripts() throws Exception {
        String input =
            "<p>Hello World</p>"
                    + "<script language=\"text/javascript\">alert(\"bad\");</script>";
        String sanitized = HTMLSanitizer.sanitize(input);
        assertEquals("<p>Hello World</p>", sanitized);
    }

    public static final void testSanitizeRemovesOnclick() throws Exception {
        String input = "<p onclick=\"alert(\"bad\");\">Hello World</p>";
        String sanitized = HTMLSanitizer.sanitize(input);
        assertEquals("<p>Hello World</p>", sanitized);
    }

    public static final void testTextAllowedInLinks() throws Exception {
        String input = "<a href=\"../good.html\">click here</a>";
        String sanitized = HTMLSanitizer.sanitize(input);
        assertEquals("<a href=\"../good.html\" rel=\"nofollow\">click here</a>",sanitized);
    }
}
