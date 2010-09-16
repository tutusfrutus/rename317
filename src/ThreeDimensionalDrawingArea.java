// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class ThreeDimensionalDrawingArea extends DrawingArea {

    public static void nullLoader()
    {
        anIntArray1468 = null;
        anIntArray1468 = null;
        SINE = null;
        COSINE = null;
        lineOffsets = null;
        textureImages = null;
        texture_is_transparent = null;
        anIntArray1476 = null;
        texture_pixel_array_pool = null;
        texture_pixel_cache = null;
        texture_last_used = null;
        colourMap = null;
        texturePalettes = null;
    }

    public static void initToActiveDrawingArea()
    {
        lineOffsets = new int[DrawingArea.height];
        for(int j = 0; j < DrawingArea.height; j++)
            lineOffsets[j] = DrawingArea.width * j;

        xMidPos = DrawingArea.width / 2;
        yMidPos = DrawingArea.height / 2;
    }

    public static void initToDimensions(int width, int height)
    {
       lineOffsets = new int[height];
        for(int l = 0; l < height; l++)
            lineOffsets[l] = width * l;

        xMidPos = width / 2;
        yMidPos = height / 2;
    }

    public static void nullInit()
    {
        texture_pixel_array_pool = null;
        for(int j = 0; j < 50; j++)
            texture_pixel_cache[j] = null;

    }

    public static void cleanup()
    {
        if(texture_pixel_array_pool == null)
        {
            texture_pixel_array_pool_ptr = 20;//was parameter
            if(lowMem)
                texture_pixel_array_pool = new int[texture_pixel_array_pool_ptr][16384];
            else
                texture_pixel_array_pool = new int[texture_pixel_array_pool_ptr][0x10000];
            for(int k = 0; k < 50; k++)
                texture_pixel_cache[k] = null;

        }
    }

    public static void loadTextures(JagexArchive jagexArchive)
    {
        loadedTextureCount = 0;
        for(int j = 0; j < 50; j++)
            try
            {
                textureImages[j] = new IndexedImage(jagexArchive, String.valueOf(j), 0);
                if(lowMem && textureImages[j].libWidth == 128)
                    textureImages[j].resizeToHalfLibSize();
                else
                    textureImages[j].resizeToLibSize();
                loadedTextureCount++;
            }
            catch(Exception _ex) { }

    }

    public static int method369(int i)
    {
        if(anIntArray1476[i] != 0)
            return anIntArray1476[i];
        int k = 0;
        int l = 0;
        int i1 = 0;
        int j1 = texturePalettes[i].length;
        for(int k1 = 0; k1 < j1; k1++)
        {
            k += texturePalettes[i][k1] >> 16 & 0xff;
            l += texturePalettes[i][k1] >> 8 & 0xff;
            i1 += texturePalettes[i][k1] & 0xff;
        }

        int l1 = (k / j1 << 16) + (l / j1 << 8) + i1 / j1;
        l1 = applyBrightnessToRGB(l1, 1.3999999999999999D);
        if(l1 == 0)
            l1 = 1;
        anIntArray1476[i] = l1;
        return l1;
    }

    public static void free_texture(int texID)
    {
        if(texture_pixel_cache[texID] == null)
            return;
        texture_pixel_array_pool[texture_pixel_array_pool_ptr++] = texture_pixel_cache[texID];
        texture_pixel_cache[texID] = null;
    }

    private static int[] getTexturePixels(int tex_id)
    {
        texture_last_used[tex_id] = texture_get_count++;
        if(texture_pixel_cache[tex_id] != null)
            return texture_pixel_cache[tex_id];
        int texture_pixel_data[];
        //Start of mem management code
        if(texture_pixel_array_pool_ptr > 0)
        {	//Freed texture data arrays available
            texture_pixel_data = texture_pixel_array_pool[--texture_pixel_array_pool_ptr];
            texture_pixel_array_pool[texture_pixel_array_pool_ptr] = null;
        } else
        {   //No freed texture data arrays available, recycle least used texture's array
            int lowest_usage_count = 0;
            int cached_texture_to_use = -1;
            for(int l = 0; l < loadedTextureCount; l++)
                if(texture_pixel_cache[l] != null && (texture_last_used[l] < lowest_usage_count || cached_texture_to_use == -1))
                {
                    lowest_usage_count = texture_last_used[l];
                    cached_texture_to_use = l;
                }

            texture_pixel_data = texture_pixel_cache[cached_texture_to_use];
            texture_pixel_cache[cached_texture_to_use] = null;
        }
        texture_pixel_cache[tex_id] = texture_pixel_data;
        //End of mem management code
        IndexedImage indexedImage = textureImages[tex_id];
        int ai1[] = texturePalettes[tex_id];
        if(lowMem)
        {
            texture_is_transparent[tex_id] = false;
            for(int i1 = 0; i1 < 4096; i1++)
            {
                int i2 = texture_pixel_data[i1] = ai1[indexedImage.imgPixels[i1]] & 0xf8f8ff;
                if(i2 == 0)
                    texture_is_transparent[tex_id] = true;
                texture_pixel_data[4096 + i1] = i2 - (i2 >>> 3) & 0xf8f8ff;
                texture_pixel_data[8192 + i1] = i2 - (i2 >>> 2) & 0xf8f8ff;
                texture_pixel_data[12288 + i1] = i2 - (i2 >>> 2) - (i2 >>> 3) & 0xf8f8ff;
            }

        } else
        {
            if(indexedImage.imgWidth == 64)
            {
                for(int j1 = 0; j1 < 128; j1++)
                {
                    for(int j2 = 0; j2 < 128; j2++)
                        texture_pixel_data[j2 + (j1 << 7)] = ai1[indexedImage.imgPixels[(j2 >> 1) + ((j1 >> 1) << 6)]];

                }

            } else
            {
                for(int k1 = 0; k1 < 16384; k1++)
                    texture_pixel_data[k1] = ai1[indexedImage.imgPixels[k1]];

            }
            texture_is_transparent[tex_id] = false;
            for(int l1 = 0; l1 < 16384; l1++)
            {
                texture_pixel_data[l1] &= 0xf8f8ff;
                int k2 = texture_pixel_data[l1];
                if(k2 == 0)
                    texture_is_transparent[tex_id] = true;
                texture_pixel_data[16384 + l1] = k2 - (k2 >>> 3) & 0xf8f8ff;
                texture_pixel_data[32768 + l1] = k2 - (k2 >>> 2) & 0xf8f8ff;
                texture_pixel_data[49152 + l1] = k2 - (k2 >>> 2) - (k2 >>> 3) & 0xf8f8ff;
            }

        }
        return texture_pixel_data;
    }

    public static void setBrightness(double brightness)
    {
        brightness += Math.random() * 0.029999999999999999D - 0.014999999999999999D;
        int j = 0;
        for(int k = 0; k < 512; k++)
        {
            double d1 = (double)(k / 8) / 64D + 0.0078125D;
            double d2 = (double)(k & 7) / 8D + 0.0625D;
            for(int k1 = 0; k1 < 128; k1++)
            {
                double d3 = (double)k1 / 128D;
                double d4 = d3;
                double d5 = d3;
                double d6 = d3;
                if(d2 != 0.0D)
                {
                    double d7;
                    if(d3 < 0.5D)
                        d7 = d3 * (1.0D + d2);
                    else
                        d7 = (d3 + d2) - d3 * d2;
                    double d8 = 2D * d3 - d7;
                    double d9 = d1 + 0.33333333333333331D;
                    if(d9 > 1.0D)
                        d9--;
                    double d10 = d1;
                    double d11 = d1 - 0.33333333333333331D;
                    if(d11 < 0.0D)
                        d11++;
                    if(6D * d9 < 1.0D)
                        d4 = d8 + (d7 - d8) * 6D * d9;
                    else
                    if(2D * d9 < 1.0D)
                        d4 = d7;
                    else
                    if(3D * d9 < 2D)
                        d4 = d8 + (d7 - d8) * (0.66666666666666663D - d9) * 6D;
                    else
                        d4 = d8;
                    if(6D * d10 < 1.0D)
                        d5 = d8 + (d7 - d8) * 6D * d10;
                    else
                    if(2D * d10 < 1.0D)
                        d5 = d7;
                    else
                    if(3D * d10 < 2D)
                        d5 = d8 + (d7 - d8) * (0.66666666666666663D - d10) * 6D;
                    else
                        d5 = d8;
                    if(6D * d11 < 1.0D)
                        d6 = d8 + (d7 - d8) * 6D * d11;
                    else
                    if(2D * d11 < 1.0D)
                        d6 = d7;
                    else
                    if(3D * d11 < 2D)
                        d6 = d8 + (d7 - d8) * (0.66666666666666663D - d11) * 6D;
                    else
                        d6 = d8;
                }
                int l1 = (int)(d4 * 256D);
                int i2 = (int)(d5 * 256D);
                int j2 = (int)(d6 * 256D);
                int rgb = (l1 << 16) + (i2 << 8) + j2;
                rgb = applyBrightnessToRGB(rgb, brightness);
                if(rgb == 0)
                    rgb = 1;
                colourMap[j++] = rgb;
            }

        }

        for(int l = 0; l < 50; l++)
            if(textureImages[l] != null)
            {
                int ai[] = textureImages[l].palette;
                texturePalettes[l] = new int[ai.length];
                for(int j1 = 0; j1 < ai.length; j1++)
                {
                    texturePalettes[l][j1] = applyBrightnessToRGB(ai[j1], brightness);
                    if((texturePalettes[l][j1] & 0xf8f8ff) == 0 && j1 != 0)
                        texturePalettes[l][j1] = 1;
                }

            }

        for(int i1 = 0; i1 < 50; i1++)
            free_texture(i1);

    }

    private static int applyBrightnessToRGB(int i, double d)
    {
        double r = (double)(i >> 16) / 256D;
        double g = (double)(i >> 8 & 0xff) / 256D;
        double b = (double)(i & 0xff) / 256D;
        r = Math.pow(r, d);
        g = Math.pow(g, d);
        b = Math.pow(b, d);
        int r_byte = (int)(r * 256D);
        int g_byte = (int)(g * 256D);
        int b_byte = (int)(b * 256D);
        return (r_byte << 16) + (g_byte << 8) + b_byte;
    }

    public static void drawColouredTriangle(int i, int j, int k, int l, int i1, int j1, int k1, int l1,
            int i2)
    {
        int j2 = 0;
        int k2 = 0;
        if(j != i)
        {
            j2 = (i1 - l << 16) / (j - i);
            k2 = (l1 - k1 << 15) / (j - i);
        }
        int l2 = 0;
        int i3 = 0;
        if(k != j)
        {
            l2 = (j1 - i1 << 16) / (k - j);
            i3 = (i2 - l1 << 15) / (k - j);
        }
        int j3 = 0;
        int k3 = 0;
        if(k != i)
        {
            j3 = (l - j1 << 16) / (i - k);
            k3 = (k1 - i2 << 15) / (i - k);
        }
        if(i <= j && i <= k)
        {
            if(i >= DrawingArea.viewport_h)
                return;
            if(j > DrawingArea.viewport_h)
                j = DrawingArea.viewport_h;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(j < k)
            {
                j1 = l <<= 16;
                i2 = k1 <<= 15;
                if(i < 0)
                {
                    j1 -= j3 * i;
                    l -= j2 * i;
                    i2 -= k3 * i;
                    k1 -= k2 * i;
                    i = 0;
                }
                i1 <<= 16;
                l1 <<= 15;
                if(j < 0)
                {
                    i1 -= l2 * j;
                    l1 -= i3 * j;
                    j = 0;
                }
                if(i != j && j3 < j2 || i == j && j3 > l2)
                {
                    k -= j;
                    j -= i;
                    for(i = lineOffsets[i]; --j >= 0; i += DrawingArea.width)
                    {
                        draw2DJagColouredShape(DrawingArea.pixels, i, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
                        j1 += j3;
                        l += j2;
                        i2 += k3;
                        k1 += k2;
                    }

                    while(--k >= 0)
                    {
                        draw2DJagColouredShape(DrawingArea.pixels, i, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
                        j1 += j3;
                        i1 += l2;
                        i2 += k3;
                        l1 += i3;
                        i += DrawingArea.width;
                    }
                    return;
                }
                k -= j;
                j -= i;
                for(i = lineOffsets[i]; --j >= 0; i += DrawingArea.width)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, i, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
                    j1 += j3;
                    l += j2;
                    i2 += k3;
                    k1 += k2;
                }

                while(--k >= 0)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, i, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
                    j1 += j3;
                    i1 += l2;
                    i2 += k3;
                    l1 += i3;
                    i += DrawingArea.width;
                }
                return;
            }
            i1 = l <<= 16;
            l1 = k1 <<= 15;
            if(i < 0)
            {
                i1 -= j3 * i;
                l -= j2 * i;
                l1 -= k3 * i;
                k1 -= k2 * i;
                i = 0;
            }
            j1 <<= 16;
            i2 <<= 15;
            if(k < 0)
            {
                j1 -= l2 * k;
                i2 -= i3 * k;
                k = 0;
            }
            if(i != k && j3 < j2 || i == k && l2 > j2)
            {
                j -= k;
                k -= i;
                for(i = lineOffsets[i]; --k >= 0; i += DrawingArea.width)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, i, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
                    i1 += j3;
                    l += j2;
                    l1 += k3;
                    k1 += k2;
                }

                while(--j >= 0)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, i, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
                    j1 += l2;
                    l += j2;
                    i2 += i3;
                    k1 += k2;
                    i += DrawingArea.width;
                }
                return;
            }
            j -= k;
            k -= i;
            for(i = lineOffsets[i]; --k >= 0; i += DrawingArea.width)
            {
                draw2DJagColouredShape(DrawingArea.pixels, i, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
                i1 += j3;
                l += j2;
                l1 += k3;
                k1 += k2;
            }

            while(--j >= 0)
            {
                draw2DJagColouredShape(DrawingArea.pixels, i, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
                j1 += l2;
                l += j2;
                i2 += i3;
                k1 += k2;
                i += DrawingArea.width;
            }
            return;
        }
        if(j <= k)
        {
            if(j >= DrawingArea.viewport_h)
                return;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(i > DrawingArea.viewport_h)
                i = DrawingArea.viewport_h;
            if(k < i)
            {
                l = i1 <<= 16;
                k1 = l1 <<= 15;
                if(j < 0)
                {
                    l -= j2 * j;
                    i1 -= l2 * j;
                    k1 -= k2 * j;
                    l1 -= i3 * j;
                    j = 0;
                }
                j1 <<= 16;
                i2 <<= 15;
                if(k < 0)
                {
                    j1 -= j3 * k;
                    i2 -= k3 * k;
                    k = 0;
                }
                if(j != k && j2 < l2 || j == k && j2 > j3)
                {
                    i -= k;
                    k -= j;
                    for(j = lineOffsets[j]; --k >= 0; j += DrawingArea.width)
                    {
                        draw2DJagColouredShape(DrawingArea.pixels, j, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
                        l += j2;
                        i1 += l2;
                        k1 += k2;
                        l1 += i3;
                    }

                    while(--i >= 0)
                    {
                        draw2DJagColouredShape(DrawingArea.pixels, j, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
                        l += j2;
                        j1 += j3;
                        k1 += k2;
                        i2 += k3;
                        j += DrawingArea.width;
                    }
                    return;
                }
                i -= k;
                k -= j;
                for(j = lineOffsets[j]; --k >= 0; j += DrawingArea.width)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, j, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
                    l += j2;
                    i1 += l2;
                    k1 += k2;
                    l1 += i3;
                }

                while(--i >= 0)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, j, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
                    l += j2;
                    j1 += j3;
                    k1 += k2;
                    i2 += k3;
                    j += DrawingArea.width;
                }
                return;
            }
            j1 = i1 <<= 16;
            i2 = l1 <<= 15;
            if(j < 0)
            {
                j1 -= j2 * j;
                i1 -= l2 * j;
                i2 -= k2 * j;
                l1 -= i3 * j;
                j = 0;
            }
            l <<= 16;
            k1 <<= 15;
            if(i < 0)
            {
                l -= j3 * i;
                k1 -= k3 * i;
                i = 0;
            }
            if(j2 < l2)
            {
                k -= i;
                i -= j;
                for(j = lineOffsets[j]; --i >= 0; j += DrawingArea.width)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, j, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
                    j1 += j2;
                    i1 += l2;
                    i2 += k2;
                    l1 += i3;
                }

                while(--k >= 0)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, j, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
                    l += j3;
                    i1 += l2;
                    k1 += k3;
                    l1 += i3;
                    j += DrawingArea.width;
                }
                return;
            }
            k -= i;
            i -= j;
            for(j = lineOffsets[j]; --i >= 0; j += DrawingArea.width)
            {
                draw2DJagColouredShape(DrawingArea.pixels, j, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
                j1 += j2;
                i1 += l2;
                i2 += k2;
                l1 += i3;
            }

            while(--k >= 0)
            {
                draw2DJagColouredShape(DrawingArea.pixels, j, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
                l += j3;
                i1 += l2;
                k1 += k3;
                l1 += i3;
                j += DrawingArea.width;
            }
            return;
        }
        if(k >= DrawingArea.viewport_h)
            return;
        if(i > DrawingArea.viewport_h)
            i = DrawingArea.viewport_h;
        if(j > DrawingArea.viewport_h)
            j = DrawingArea.viewport_h;
        if(i < j)
        {
            i1 = j1 <<= 16;
            l1 = i2 <<= 15;
            if(k < 0)
            {
                i1 -= l2 * k;
                j1 -= j3 * k;
                l1 -= i3 * k;
                i2 -= k3 * k;
                k = 0;
            }
            l <<= 16;
            k1 <<= 15;
            if(i < 0)
            {
                l -= j2 * i;
                k1 -= k2 * i;
                i = 0;
            }
            if(l2 < j3)
            {
                j -= i;
                i -= k;
                for(k = lineOffsets[k]; --i >= 0; k += DrawingArea.width)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, k, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
                    i1 += l2;
                    j1 += j3;
                    l1 += i3;
                    i2 += k3;
                }

                while(--j >= 0)
                {
                    draw2DJagColouredShape(DrawingArea.pixels, k, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
                    i1 += l2;
                    l += j2;
                    l1 += i3;
                    k1 += k2;
                    k += DrawingArea.width;
                }
                return;
            }
            j -= i;
            i -= k;
            for(k = lineOffsets[k]; --i >= 0; k += DrawingArea.width)
            {
                draw2DJagColouredShape(DrawingArea.pixels, k, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
                i1 += l2;
                j1 += j3;
                l1 += i3;
                i2 += k3;
            }

            while(--j >= 0)
            {
                draw2DJagColouredShape(DrawingArea.pixels, k, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
                i1 += l2;
                l += j2;
                l1 += i3;
                k1 += k2;
                k += DrawingArea.width;
            }
            return;
        }
        l = j1 <<= 16;
        k1 = i2 <<= 15;
        if(k < 0)
        {
            l -= l2 * k;
            j1 -= j3 * k;
            k1 -= i3 * k;
            i2 -= k3 * k;
            k = 0;
        }
        i1 <<= 16;
        l1 <<= 15;
        if(j < 0)
        {
            i1 -= j2 * j;
            l1 -= k2 * j;
            j = 0;
        }
        if(l2 < j3)
        {
            i -= j;
            j -= k;
            for(k = lineOffsets[k]; --j >= 0; k += DrawingArea.width)
            {
                draw2DJagColouredShape(DrawingArea.pixels, k, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
                l += l2;
                j1 += j3;
                k1 += i3;
                i2 += k3;
            }

            while(--i >= 0)
            {
                draw2DJagColouredShape(DrawingArea.pixels, k, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
                i1 += j2;
                j1 += j3;
                l1 += k2;
                i2 += k3;
                k += DrawingArea.width;
            }
            return;
        }
        i -= j;
        j -= k;
        for(k = lineOffsets[k]; --j >= 0; k += DrawingArea.width)
        {
            draw2DJagColouredShape(DrawingArea.pixels, k, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
            l += l2;
            j1 += j3;
            k1 += i3;
            i2 += k3;
        }

        while(--i >= 0)
        {
            draw2DJagColouredShape(DrawingArea.pixels, k, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
            i1 += j2;
            j1 += j3;
            l1 += k2;
            i2 += k3;
            k += DrawingArea.width;
        }
    }

    private static void draw2DJagColouredShape(int ai[], int i, int l, int i1, int jgx, int k1)
    {
        int rgb;//was parameter
        int k;//was parameter
        if(aBoolean1464)
        {
            int l1;
            if(aBoolean1462)
            {
                if(i1 - l > 3)
                    l1 = (k1 - jgx) / (i1 - l);
                else
                    l1 = 0;
                if(i1 > DrawingArea.viewport_r_x)
                    i1 = DrawingArea.viewport_r_x;
                if(l < 0)
                {
                    jgx -= l * l1;
                    l = 0;
                }
                if(l >= i1)
                    return;
                i += l;
                k = i1 - l >> 2;
                l1 <<= 2;
            } else
            {
                if(l >= i1)
                    return;
                i += l;
                k = i1 - l >> 2;
                if(k > 0)
                    l1 = (k1 - jgx) * anIntArray1468[k] >> 15;
                else
                    l1 = 0;
            }
            if(alpha == 0)
            {
                while(--k >= 0) 
                {
                    rgb = colourMap[jgx >> 8];
                    jgx += l1;
                    ai[i++] = rgb;
                    ai[i++] = rgb;
                    ai[i++] = rgb;
                    ai[i++] = rgb;
                }
                k = i1 - l & 3;
                if(k > 0)
                {
                    rgb = colourMap[jgx >> 8];
                    do
                        ai[i++] = rgb;
                    while(--k > 0);
                    return;
                }
            } else
            {
                int j2 = alpha;
                int l2 = 256 - alpha;
                while(--k >= 0) 
                {
                    rgb = colourMap[jgx >> 8];
                    jgx += l1;
                    rgb = ((rgb & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((rgb & 0xff00) * l2 >> 8 & 0xff00);
                    ai[i++] = rgb + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
                    ai[i++] = rgb + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
                    ai[i++] = rgb + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
                    ai[i++] = rgb + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
                }
                k = i1 - l & 3;
                if(k > 0)
                {
                    rgb = colourMap[jgx >> 8];
                    rgb = ((rgb & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((rgb & 0xff00) * l2 >> 8 & 0xff00);
                    do
                        ai[i++] = rgb + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
                    while(--k > 0);
                }
            }
            return;
        }
        if(l >= i1)
            return;
        int i2 = (k1 - jgx) / (i1 - l);
        if(aBoolean1462)
        {
            if(i1 > DrawingArea.viewport_r_x)
                i1 = DrawingArea.viewport_r_x;
            if(l < 0)
            {
                jgx -= l * i2;
                l = 0;
            }
            if(l >= i1)
                return;
        }
        i += l;
        k = i1 - l;
        if(alpha == 0)
        {
            do
            {
                ai[i++] = colourMap[jgx >> 8];
                jgx += i2;
            } while(--k > 0);
            return;
        }
        int k2 = alpha;
        int i3 = 256 - alpha;
        do
        {
            rgb = colourMap[jgx >> 8];
            jgx += i2;
            rgb = ((rgb & 0xff00ff) * i3 >> 8 & 0xff00ff) + ((rgb & 0xff00) * i3 >> 8 & 0xff00);
            ai[i++] = rgb + ((ai[i] & 0xff00ff) * k2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * k2 >> 8 & 0xff00);
        } while(--k > 0);
    }

    public static void method376(int i, int j, int k, int l, int i1, int j1, int k1)
    {
        int l1 = 0;
        if(j != i)
            l1 = (i1 - l << 16) / (j - i);
        int i2 = 0;
        if(k != j)
            i2 = (j1 - i1 << 16) / (k - j);
        int j2 = 0;
        if(k != i)
            j2 = (l - j1 << 16) / (i - k);
        if(i <= j && i <= k)
        {
            if(i >= DrawingArea.viewport_h)
                return;
            if(j > DrawingArea.viewport_h)
                j = DrawingArea.viewport_h;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(j < k)
            {
                j1 = l <<= 16;
                if(i < 0)
                {
                    j1 -= j2 * i;
                    l -= l1 * i;
                    i = 0;
                }
                i1 <<= 16;
                if(j < 0)
                {
                    i1 -= i2 * j;
                    j = 0;
                }
                if(i != j && j2 < l1 || i == j && j2 > i2)
                {
                    k -= j;
                    j -= i;
                    for(i = lineOffsets[i]; --j >= 0; i += DrawingArea.width)
                    {
                        method377(DrawingArea.pixels, i, k1, j1 >> 16, l >> 16);
                        j1 += j2;
                        l += l1;
                    }

                    while(--k >= 0) 
                    {
                        method377(DrawingArea.pixels, i, k1, j1 >> 16, i1 >> 16);
                        j1 += j2;
                        i1 += i2;
                        i += DrawingArea.width;
                    }
                    return;
                }
                k -= j;
                j -= i;
                for(i = lineOffsets[i]; --j >= 0; i += DrawingArea.width)
                {
                    method377(DrawingArea.pixels, i, k1, l >> 16, j1 >> 16);
                    j1 += j2;
                    l += l1;
                }

                while(--k >= 0) 
                {
                    method377(DrawingArea.pixels, i, k1, i1 >> 16, j1 >> 16);
                    j1 += j2;
                    i1 += i2;
                    i += DrawingArea.width;
                }
                return;
            }
            i1 = l <<= 16;
            if(i < 0)
            {
                i1 -= j2 * i;
                l -= l1 * i;
                i = 0;
            }
            j1 <<= 16;
            if(k < 0)
            {
                j1 -= i2 * k;
                k = 0;
            }
            if(i != k && j2 < l1 || i == k && i2 > l1)
            {
                j -= k;
                k -= i;
                for(i = lineOffsets[i]; --k >= 0; i += DrawingArea.width)
                {
                    method377(DrawingArea.pixels, i, k1, i1 >> 16, l >> 16);
                    i1 += j2;
                    l += l1;
                }

                while(--j >= 0) 
                {
                    method377(DrawingArea.pixels, i, k1, j1 >> 16, l >> 16);
                    j1 += i2;
                    l += l1;
                    i += DrawingArea.width;
                }
                return;
            }
            j -= k;
            k -= i;
            for(i = lineOffsets[i]; --k >= 0; i += DrawingArea.width)
            {
                method377(DrawingArea.pixels, i, k1, l >> 16, i1 >> 16);
                i1 += j2;
                l += l1;
            }

            while(--j >= 0) 
            {
                method377(DrawingArea.pixels, i, k1, l >> 16, j1 >> 16);
                j1 += i2;
                l += l1;
                i += DrawingArea.width;
            }
            return;
        }
        if(j <= k)
        {
            if(j >= DrawingArea.viewport_h)
                return;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(i > DrawingArea.viewport_h)
                i = DrawingArea.viewport_h;
            if(k < i)
            {
                l = i1 <<= 16;
                if(j < 0)
                {
                    l -= l1 * j;
                    i1 -= i2 * j;
                    j = 0;
                }
                j1 <<= 16;
                if(k < 0)
                {
                    j1 -= j2 * k;
                    k = 0;
                }
                if(j != k && l1 < i2 || j == k && l1 > j2)
                {
                    i -= k;
                    k -= j;
                    for(j = lineOffsets[j]; --k >= 0; j += DrawingArea.width)
                    {
                        method377(DrawingArea.pixels, j, k1, l >> 16, i1 >> 16);
                        l += l1;
                        i1 += i2;
                    }

                    while(--i >= 0) 
                    {
                        method377(DrawingArea.pixels, j, k1, l >> 16, j1 >> 16);
                        l += l1;
                        j1 += j2;
                        j += DrawingArea.width;
                    }
                    return;
                }
                i -= k;
                k -= j;
                for(j = lineOffsets[j]; --k >= 0; j += DrawingArea.width)
                {
                    method377(DrawingArea.pixels, j, k1, i1 >> 16, l >> 16);
                    l += l1;
                    i1 += i2;
                }

                while(--i >= 0) 
                {
                    method377(DrawingArea.pixels, j, k1, j1 >> 16, l >> 16);
                    l += l1;
                    j1 += j2;
                    j += DrawingArea.width;
                }
                return;
            }
            j1 = i1 <<= 16;
            if(j < 0)
            {
                j1 -= l1 * j;
                i1 -= i2 * j;
                j = 0;
            }
            l <<= 16;
            if(i < 0)
            {
                l -= j2 * i;
                i = 0;
            }
            if(l1 < i2)
            {
                k -= i;
                i -= j;
                for(j = lineOffsets[j]; --i >= 0; j += DrawingArea.width)
                {
                    method377(DrawingArea.pixels, j, k1, j1 >> 16, i1 >> 16);
                    j1 += l1;
                    i1 += i2;
                }

                while(--k >= 0) 
                {
                    method377(DrawingArea.pixels, j, k1, l >> 16, i1 >> 16);
                    l += j2;
                    i1 += i2;
                    j += DrawingArea.width;
                }
                return;
            }
            k -= i;
            i -= j;
            for(j = lineOffsets[j]; --i >= 0; j += DrawingArea.width)
            {
                method377(DrawingArea.pixels, j, k1, i1 >> 16, j1 >> 16);
                j1 += l1;
                i1 += i2;
            }

            while(--k >= 0) 
            {
                method377(DrawingArea.pixels, j, k1, i1 >> 16, l >> 16);
                l += j2;
                i1 += i2;
                j += DrawingArea.width;
            }
            return;
        }
        if(k >= DrawingArea.viewport_h)
            return;
        if(i > DrawingArea.viewport_h)
            i = DrawingArea.viewport_h;
        if(j > DrawingArea.viewport_h)
            j = DrawingArea.viewport_h;
        if(i < j)
        {
            i1 = j1 <<= 16;
            if(k < 0)
            {
                i1 -= i2 * k;
                j1 -= j2 * k;
                k = 0;
            }
            l <<= 16;
            if(i < 0)
            {
                l -= l1 * i;
                i = 0;
            }
            if(i2 < j2)
            {
                j -= i;
                i -= k;
                for(k = lineOffsets[k]; --i >= 0; k += DrawingArea.width)
                {
                    method377(DrawingArea.pixels, k, k1, i1 >> 16, j1 >> 16);
                    i1 += i2;
                    j1 += j2;
                }

                while(--j >= 0) 
                {
                    method377(DrawingArea.pixels, k, k1, i1 >> 16, l >> 16);
                    i1 += i2;
                    l += l1;
                    k += DrawingArea.width;
                }
                return;
            }
            j -= i;
            i -= k;
            for(k = lineOffsets[k]; --i >= 0; k += DrawingArea.width)
            {
                method377(DrawingArea.pixels, k, k1, j1 >> 16, i1 >> 16);
                i1 += i2;
                j1 += j2;
            }

            while(--j >= 0) 
            {
                method377(DrawingArea.pixels, k, k1, l >> 16, i1 >> 16);
                i1 += i2;
                l += l1;
                k += DrawingArea.width;
            }
            return;
        }
        l = j1 <<= 16;
        if(k < 0)
        {
            l -= i2 * k;
            j1 -= j2 * k;
            k = 0;
        }
        i1 <<= 16;
        if(j < 0)
        {
            i1 -= l1 * j;
            j = 0;
        }
        if(i2 < j2)
        {
            i -= j;
            j -= k;
            for(k = lineOffsets[k]; --j >= 0; k += DrawingArea.width)
            {
                method377(DrawingArea.pixels, k, k1, l >> 16, j1 >> 16);
                l += i2;
                j1 += j2;
            }

            while(--i >= 0) 
            {
                method377(DrawingArea.pixels, k, k1, i1 >> 16, j1 >> 16);
                i1 += l1;
                j1 += j2;
                k += DrawingArea.width;
            }
            return;
        }
        i -= j;
        j -= k;
        for(k = lineOffsets[k]; --j >= 0; k += DrawingArea.width)
        {
            method377(DrawingArea.pixels, k, k1, j1 >> 16, l >> 16);
            l += i2;
            j1 += j2;
        }

        while(--i >= 0) 
        {
            method377(DrawingArea.pixels, k, k1, j1 >> 16, i1 >> 16);
            i1 += l1;
            j1 += j2;
            k += DrawingArea.width;
        }
    }

    private static void method377(int ai[], int i, int j, int l, int i1)
    {
        int k;//was parameter
        if(aBoolean1462)
        {
            if(i1 > DrawingArea.viewport_r_x)
                i1 = DrawingArea.viewport_r_x;
            if(l < 0)
                l = 0;
        }
        if(l >= i1)
            return;
        i += l;
        k = i1 - l >> 2;
        if(alpha == 0)
        {
            while(--k >= 0) 
            {
                ai[i++] = j;
                ai[i++] = j;
                ai[i++] = j;
                ai[i++] = j;
            }
            for(k = i1 - l & 3; --k >= 0;)
                ai[i++] = j;

            return;
        }
        int j1 = alpha;
        int k1 = 256 - alpha;
        j = ((j & 0xff00ff) * k1 >> 8 & 0xff00ff) + ((j & 0xff00) * k1 >> 8 & 0xff00);
        while(--k >= 0) 
        {
            ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
            ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
            ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
            ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
        }
        for(k = i1 - l & 3; --k >= 0;)
            ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);

    }

    public static void drawTexturedTriangle(int i, int j, int k, int l, int i1, int j1, int k1, int l1,
            int i2, int j2, int k2, int l2, int i3, int j3, int k3, 
            int l3, int i4, int j4, int k4)
    {
        int ai[] = getTexturePixels(k4);
        aBoolean1463 = !texture_is_transparent[k4];
        k2 = j2 - k2;
        j3 = i3 - j3;
        i4 = l3 - i4;
        l2 -= j2;
        k3 -= i3;
        j4 -= l3;
        int l4 = l2 * i3 - k3 * j2 << 14;
        int i5 = k3 * l3 - j4 * i3 << 8;
        int j5 = j4 * j2 - l2 * l3 << 5;
        int k5 = k2 * i3 - j3 * j2 << 14;
        int l5 = j3 * l3 - i4 * i3 << 8;
        int i6 = i4 * j2 - k2 * l3 << 5;
        int j6 = j3 * l2 - k2 * k3 << 14;
        int k6 = i4 * k3 - j3 * j4 << 8;
        int l6 = k2 * j4 - i4 * l2 << 5;
        int i7 = 0;
        int j7 = 0;
        if(j != i)
        {
            i7 = (i1 - l << 16) / (j - i);
            j7 = (l1 - k1 << 16) / (j - i);
        }
        int k7 = 0;
        int l7 = 0;
        if(k != j)
        {
            k7 = (j1 - i1 << 16) / (k - j);
            l7 = (i2 - l1 << 16) / (k - j);
        }
        int i8 = 0;
        int j8 = 0;
        if(k != i)
        {
            i8 = (l - j1 << 16) / (i - k);
            j8 = (k1 - i2 << 16) / (i - k);
        }
        if(i <= j && i <= k)
        {
            if(i >= DrawingArea.viewport_h)
                return;
            if(j > DrawingArea.viewport_h)
                j = DrawingArea.viewport_h;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(j < k)
            {
                j1 = l <<= 16;
                i2 = k1 <<= 16;
                if(i < 0)
                {
                    j1 -= i8 * i;
                    l -= i7 * i;
                    i2 -= j8 * i;
                    k1 -= j7 * i;
                    i = 0;
                }
                i1 <<= 16;
                l1 <<= 16;
                if(j < 0)
                {
                    i1 -= k7 * j;
                    l1 -= l7 * j;
                    j = 0;
                }
                int k8 = i - yMidPos;
                l4 += j5 * k8;
                k5 += i6 * k8;
                j6 += l6 * k8;
                if(i != j && i8 < i7 || i == j && i8 > k7)
                {
                    k -= j;
                    j -= i;
                    i = lineOffsets[i];
                    while(--j >= 0) 
                    {
                        method379(DrawingArea.pixels, ai, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                        j1 += i8;
                        l += i7;
                        i2 += j8;
                        k1 += j7;
                        i += DrawingArea.width;
                        l4 += j5;
                        k5 += i6;
                        j6 += l6;
                    }
                    while(--k >= 0) 
                    {
                        method379(DrawingArea.pixels, ai, i, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                        j1 += i8;
                        i1 += k7;
                        i2 += j8;
                        l1 += l7;
                        i += DrawingArea.width;
                        l4 += j5;
                        k5 += i6;
                        j6 += l6;
                    }
                    return;
                }
                k -= j;
                j -= i;
                i = lineOffsets[i];
                while(--j >= 0) 
                {
                    method379(DrawingArea.pixels, ai, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                    j1 += i8;
                    l += i7;
                    i2 += j8;
                    k1 += j7;
                    i += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                while(--k >= 0) 
                {
                    method379(DrawingArea.pixels, ai, i, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                    j1 += i8;
                    i1 += k7;
                    i2 += j8;
                    l1 += l7;
                    i += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                return;
            }
            i1 = l <<= 16;
            l1 = k1 <<= 16;
            if(i < 0)
            {
                i1 -= i8 * i;
                l -= i7 * i;
                l1 -= j8 * i;
                k1 -= j7 * i;
                i = 0;
            }
            j1 <<= 16;
            i2 <<= 16;
            if(k < 0)
            {
                j1 -= k7 * k;
                i2 -= l7 * k;
                k = 0;
            }
            int l8 = i - yMidPos;
            l4 += j5 * l8;
            k5 += i6 * l8;
            j6 += l6 * l8;
            if(i != k && i8 < i7 || i == k && k7 > i7)
            {
                j -= k;
                k -= i;
                i = lineOffsets[i];
                while(--k >= 0) 
                {
                    method379(DrawingArea.pixels, ai, i, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                    i1 += i8;
                    l += i7;
                    l1 += j8;
                    k1 += j7;
                    i += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                while(--j >= 0) 
                {
                    method379(DrawingArea.pixels, ai, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                    j1 += k7;
                    l += i7;
                    i2 += l7;
                    k1 += j7;
                    i += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                return;
            }
            j -= k;
            k -= i;
            i = lineOffsets[i];
            while(--k >= 0) 
            {
                method379(DrawingArea.pixels, ai, i, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                i1 += i8;
                l += i7;
                l1 += j8;
                k1 += j7;
                i += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            while(--j >= 0) 
            {
                method379(DrawingArea.pixels, ai, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                j1 += k7;
                l += i7;
                i2 += l7;
                k1 += j7;
                i += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            return;
        }
        if(j <= k)
        {
            if(j >= DrawingArea.viewport_h)
                return;
            if(k > DrawingArea.viewport_h)
                k = DrawingArea.viewport_h;
            if(i > DrawingArea.viewport_h)
                i = DrawingArea.viewport_h;
            if(k < i)
            {
                l = i1 <<= 16;
                k1 = l1 <<= 16;
                if(j < 0)
                {
                    l -= i7 * j;
                    i1 -= k7 * j;
                    k1 -= j7 * j;
                    l1 -= l7 * j;
                    j = 0;
                }
                j1 <<= 16;
                i2 <<= 16;
                if(k < 0)
                {
                    j1 -= i8 * k;
                    i2 -= j8 * k;
                    k = 0;
                }
                int i9 = j - yMidPos;
                l4 += j5 * i9;
                k5 += i6 * i9;
                j6 += l6 * i9;
                if(j != k && i7 < k7 || j == k && i7 > i8)
                {
                    i -= k;
                    k -= j;
                    j = lineOffsets[j];
                    while(--k >= 0) 
                    {
                        method379(DrawingArea.pixels, ai, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                        l += i7;
                        i1 += k7;
                        k1 += j7;
                        l1 += l7;
                        j += DrawingArea.width;
                        l4 += j5;
                        k5 += i6;
                        j6 += l6;
                    }
                    while(--i >= 0) 
                    {
                        method379(DrawingArea.pixels, ai, j, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                        l += i7;
                        j1 += i8;
                        k1 += j7;
                        i2 += j8;
                        j += DrawingArea.width;
                        l4 += j5;
                        k5 += i6;
                        j6 += l6;
                    }
                    return;
                }
                i -= k;
                k -= j;
                j = lineOffsets[j];
                while(--k >= 0) 
                {
                    method379(DrawingArea.pixels, ai, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                    l += i7;
                    i1 += k7;
                    k1 += j7;
                    l1 += l7;
                    j += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                while(--i >= 0) 
                {
                    method379(DrawingArea.pixels, ai, j, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                    l += i7;
                    j1 += i8;
                    k1 += j7;
                    i2 += j8;
                    j += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                return;
            }
            j1 = i1 <<= 16;
            i2 = l1 <<= 16;
            if(j < 0)
            {
                j1 -= i7 * j;
                i1 -= k7 * j;
                i2 -= j7 * j;
                l1 -= l7 * j;
                j = 0;
            }
            l <<= 16;
            k1 <<= 16;
            if(i < 0)
            {
                l -= i8 * i;
                k1 -= j8 * i;
                i = 0;
            }
            int j9 = j - yMidPos;
            l4 += j5 * j9;
            k5 += i6 * j9;
            j6 += l6 * j9;
            if(i7 < k7)
            {
                k -= i;
                i -= j;
                j = lineOffsets[j];
                while(--i >= 0) 
                {
                    method379(DrawingArea.pixels, ai, j, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                    j1 += i7;
                    i1 += k7;
                    i2 += j7;
                    l1 += l7;
                    j += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                while(--k >= 0) 
                {
                    method379(DrawingArea.pixels, ai, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                    l += i8;
                    i1 += k7;
                    k1 += j8;
                    l1 += l7;
                    j += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                return;
            }
            k -= i;
            i -= j;
            j = lineOffsets[j];
            while(--i >= 0) 
            {
                method379(DrawingArea.pixels, ai, j, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                j1 += i7;
                i1 += k7;
                i2 += j7;
                l1 += l7;
                j += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            while(--k >= 0) 
            {
                method379(DrawingArea.pixels, ai, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                l += i8;
                i1 += k7;
                k1 += j8;
                l1 += l7;
                j += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            return;
        }
        if(k >= DrawingArea.viewport_h)
            return;
        if(i > DrawingArea.viewport_h)
            i = DrawingArea.viewport_h;
        if(j > DrawingArea.viewport_h)
            j = DrawingArea.viewport_h;
        if(i < j)
        {
            i1 = j1 <<= 16;
            l1 = i2 <<= 16;
            if(k < 0)
            {
                i1 -= k7 * k;
                j1 -= i8 * k;
                l1 -= l7 * k;
                i2 -= j8 * k;
                k = 0;
            }
            l <<= 16;
            k1 <<= 16;
            if(i < 0)
            {
                l -= i7 * i;
                k1 -= j7 * i;
                i = 0;
            }
            int k9 = k - yMidPos;
            l4 += j5 * k9;
            k5 += i6 * k9;
            j6 += l6 * k9;
            if(k7 < i8)
            {
                j -= i;
                i -= k;
                k = lineOffsets[k];
                while(--i >= 0) 
                {
                    method379(DrawingArea.pixels, ai, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                    i1 += k7;
                    j1 += i8;
                    l1 += l7;
                    i2 += j8;
                    k += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                while(--j >= 0) 
                {
                    method379(DrawingArea.pixels, ai, k, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
                    i1 += k7;
                    l += i7;
                    l1 += l7;
                    k1 += j7;
                    k += DrawingArea.width;
                    l4 += j5;
                    k5 += i6;
                    j6 += l6;
                }
                return;
            }
            j -= i;
            i -= k;
            k = lineOffsets[k];
            while(--i >= 0) 
            {
                method379(DrawingArea.pixels, ai, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                i1 += k7;
                j1 += i8;
                l1 += l7;
                i2 += j8;
                k += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            while(--j >= 0) 
            {
                method379(DrawingArea.pixels, ai, k, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
                i1 += k7;
                l += i7;
                l1 += l7;
                k1 += j7;
                k += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            return;
        }
        l = j1 <<= 16;
        k1 = i2 <<= 16;
        if(k < 0)
        {
            l -= k7 * k;
            j1 -= i8 * k;
            k1 -= l7 * k;
            i2 -= j8 * k;
            k = 0;
        }
        i1 <<= 16;
        l1 <<= 16;
        if(j < 0)
        {
            i1 -= i7 * j;
            l1 -= j7 * j;
            j = 0;
        }
        int l9 = k - yMidPos;
        l4 += j5 * l9;
        k5 += i6 * l9;
        j6 += l6 * l9;
        if(k7 < i8)
        {
            i -= j;
            j -= k;
            k = lineOffsets[k];
            while(--j >= 0) 
            {
                method379(DrawingArea.pixels, ai, k, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                l += k7;
                j1 += i8;
                k1 += l7;
                i2 += j8;
                k += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            while(--i >= 0) 
            {
                method379(DrawingArea.pixels, ai, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
                i1 += i7;
                j1 += i8;
                l1 += j7;
                i2 += j8;
                k += DrawingArea.width;
                l4 += j5;
                k5 += i6;
                j6 += l6;
            }
            return;
        }
        i -= j;
        j -= k;
        k = lineOffsets[k];
        while(--j >= 0) 
        {
            method379(DrawingArea.pixels, ai, k, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
            l += k7;
            j1 += i8;
            k1 += l7;
            i2 += j8;
            k += DrawingArea.width;
            l4 += j5;
            k5 += i6;
            j6 += l6;
        }
        while(--i >= 0) 
        {
            method379(DrawingArea.pixels, ai, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
            i1 += i7;
            j1 += i8;
            l1 += j7;
            i2 += j8;
            k += DrawingArea.width;
            l4 += j5;
            k5 += i6;
            j6 += l6;
        }
    }

    private static void method379(int ai[], int ai1[], int k, int l, int i1, int j1,
                                  int k1, int l1, int i2, int j2, int k2, int l2, int i3)
    {
        int i = 0;//was parameter
        int j = 0;//was parameter
        if(l >= i1)
            return;
        int j3;
        int k3;
        if(aBoolean1462)
        {
            j3 = (k1 - j1) / (i1 - l);
            if(i1 > DrawingArea.viewport_r_x)
                i1 = DrawingArea.viewport_r_x;
            if(l < 0)
            {
                j1 -= l * j3;
                l = 0;
            }
            if(l >= i1)
                return;
            k3 = i1 - l >> 3;
            j3 <<= 12;
            j1 <<= 9;
        } else
        {
            if(i1 - l > 7)
            {
                k3 = i1 - l >> 3;
                j3 = (k1 - j1) * anIntArray1468[k3] >> 6;
            } else
            {
                k3 = 0;
                j3 = 0;
            }
            j1 <<= 9;
        }
        k += l;
        if(lowMem)
        {
            int i4 = 0;
            int k4 = 0;
            int k6 = l - xMidPos;
            l1 += (k2 >> 3) * k6;
            i2 += (l2 >> 3) * k6;
            j2 += (i3 >> 3) * k6;
            int i5 = j2 >> 12;
            if(i5 != 0)
            {
                i = l1 / i5;
                j = i2 / i5;
                if(i < 0)
                    i = 0;
                else
                if(i > 4032)
                    i = 4032;
            }
            l1 += k2;
            i2 += l2;
            j2 += i3;
            i5 = j2 >> 12;
            if(i5 != 0)
            {
                i4 = l1 / i5;
                k4 = i2 / i5;
                if(i4 < 7)
                    i4 = 7;
                else
                if(i4 > 4032)
                    i4 = 4032;
            }
            int i7 = i4 - i >> 3;
            int k7 = k4 - j >> 3;
            i += (j1 & 0x600000) >> 3;
            int i8 = j1 >> 23;
            if(aBoolean1463)
            {
                while(k3-- > 0) 
                {
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i = i4;
                    j = k4;
                    l1 += k2;
                    i2 += l2;
                    j2 += i3;
                    int j5 = j2 >> 12;
                    if(j5 != 0)
                    {
                        i4 = l1 / j5;
                        k4 = i2 / j5;
                        if(i4 < 7)
                            i4 = 7;
                        else
                        if(i4 > 4032)
                            i4 = 4032;
                    }
                    i7 = i4 - i >> 3;
                    k7 = k4 - j >> 3;
                    j1 += j3;
                    i += (j1 & 0x600000) >> 3;
                    i8 = j1 >> 23;
                }
                for(k3 = i1 - l & 7; k3-- > 0;)
                {
                    ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
                    i += i7;
                    j += k7;
                }

                return;
            }
            while(k3-- > 0) 
            {
                int k8;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i += i7;
                j += k7;
                if((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = k8;
                k++;
                i = i4;
                j = k4;
                l1 += k2;
                i2 += l2;
                j2 += i3;
                int k5 = j2 >> 12;
                if(k5 != 0)
                {
                    i4 = l1 / k5;
                    k4 = i2 / k5;
                    if(i4 < 7)
                        i4 = 7;
                    else
                    if(i4 > 4032)
                        i4 = 4032;
                }
                i7 = i4 - i >> 3;
                k7 = k4 - j >> 3;
                j1 += j3;
                i += (j1 & 0x600000) >> 3;
                i8 = j1 >> 23;
            }
            for(k3 = i1 - l & 7; k3-- > 0;)
            {
                int l8;
                if((l8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
                    ai[k] = l8;
                k++;
                i += i7;
                j += k7;
            }

            return;
        }
        int j4 = 0;
        int l4 = 0;
        int l6 = l - xMidPos;
        l1 += (k2 >> 3) * l6;
        i2 += (l2 >> 3) * l6;
        j2 += (i3 >> 3) * l6;
        int l5 = j2 >> 14;
        if(l5 != 0)
        {
            i = l1 / l5;
            j = i2 / l5;
            if(i < 0)
                i = 0;
            else
            if(i > 16256)
                i = 16256;
        }
        l1 += k2;
        i2 += l2;
        j2 += i3;
        l5 = j2 >> 14;
        if(l5 != 0)
        {
            j4 = l1 / l5;
            l4 = i2 / l5;
            if(j4 < 7)
                j4 = 7;
            else
            if(j4 > 16256)
                j4 = 16256;
        }
        int j7 = j4 - i >> 3;
        int l7 = l4 - j >> 3;
        i += j1 & 0x600000;
        int j8 = j1 >> 23;
        if(aBoolean1463)
        {
            while(k3-- > 0) 
            {
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i = j4;
                j = l4;
                l1 += k2;
                i2 += l2;
                j2 += i3;
                int i6 = j2 >> 14;
                if(i6 != 0)
                {
                    j4 = l1 / i6;
                    l4 = i2 / i6;
                    if(j4 < 7)
                        j4 = 7;
                    else
                    if(j4 > 16256)
                        j4 = 16256;
                }
                j7 = j4 - i >> 3;
                l7 = l4 - j >> 3;
                j1 += j3;
                i += j1 & 0x600000;
                j8 = j1 >> 23;
            }
            for(k3 = i1 - l & 7; k3-- > 0;)
            {
                ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
                i += j7;
                j += l7;
            }

            return;
        }
        while(k3-- > 0) 
        {
            int i9;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i += j7;
            j += l7;
            if((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = i9;
            k++;
            i = j4;
            j = l4;
            l1 += k2;
            i2 += l2;
            j2 += i3;
            int j6 = j2 >> 14;
            if(j6 != 0)
            {
                j4 = l1 / j6;
                l4 = i2 / j6;
                if(j4 < 7)
                    j4 = 7;
                else
                if(j4 > 16256)
                    j4 = 16256;
            }
            j7 = j4 - i >> 3;
            l7 = l4 - j >> 3;
            j1 += j3;
            i += j1 & 0x600000;
            j8 = j1 >> 23;
        }
        for(int l3 = i1 - l & 7; l3-- > 0;)
        {
            int j9;
            if((j9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
                ai[k] = j9;
            k++;
            i += j7;
            j += l7;
        }

    }

    public static final int anInt1459 = -477;
    public static boolean lowMem = true;
    static boolean aBoolean1462;
    private static boolean aBoolean1463;
    public static boolean aBoolean1464 = true;
    public static int alpha;
    public static int xMidPos;
    public static int yMidPos;
    private static int[] anIntArray1468;
    public static final int[] anIntArray1469;
    public static int SINE[];
    public static int COSINE[];
    public static int lineOffsets[];
    private static int loadedTextureCount;
    public static IndexedImage textureImages[] = new IndexedImage[50];
    private static boolean[] texture_is_transparent = new boolean[50];
    private static int[] anIntArray1476 = new int[50];
    private static int texture_pixel_array_pool_ptr;
    private static int[][] texture_pixel_array_pool;
    private static int[][] texture_pixel_cache = new int[50][];
    public static int texture_last_used[] = new int[50];
    public static int texture_get_count;
    public static int colourMap[] = new int[0x10000];
    private static int[][] texturePalettes = new int[50][];

    static 
    {
        anIntArray1468 = new int[512];
        anIntArray1469 = new int[2048];
        SINE = new int[2048];
        COSINE = new int[2048];
        for(int i = 1; i < 512; i++)
            anIntArray1468[i] = 32768 / i;

        for(int j = 1; j < 2048; j++)
            anIntArray1469[j] = 0x10000 / j;

        for(int k = 0; k < 2048; k++)
        {
            SINE[k] = (int)(65536D * Math.sin((double)k * 0.0030679614999999999D));
            COSINE[k] = (int)(65536D * Math.cos((double)k * 0.0030679614999999999D));
        }

    }
}
