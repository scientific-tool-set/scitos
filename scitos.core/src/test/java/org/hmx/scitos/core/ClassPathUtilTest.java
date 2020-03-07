package org.hmx.scitos.core;

import java.util.Collections;
import java.util.List;
import org.hmx.scitos.core.util.ClassPathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the {@link ClassPathUtil} class.
 */
public class ClassPathUtilTest {

    /** Test: of util's getFileResourcePaths method, matching a single file by name. */
    @Test
    public void testGetFileResourcePaths_1() {
        final List<String> result = ClassPathUtil.getFileResourcePaths(ClassPathUtilTest.class, "Empty-ClassPathUtil-File[.]txt");
        Assert.assertEquals(Collections.singletonList("/org/hmx/scitos/core/Empty-ClassPathUtil-File.txt"), result);
    }

    /** Test: of util's getFileResourcePaths method, matching two files by regular expression. */
    @Test
    public void testGetFileResourcePaths_2() {
        final List<String> result = ClassPathUtil.getFileResourcePaths(ClassPathUtilTest.class, ".+-File[.]txt");
        Assert.assertTrue(result.contains("/org/hmx/scitos/core/Empty-ClassPathUtil-File.txt"));
        Assert.assertTrue(result.contains("/org/hmx/scitos/core/Other-File.txt"));
    }
}
