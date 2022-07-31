package com.example.hikerview.ui.view.ZLoadingDialog;

/**
 * 作者：By hdy
 * 日期：On 2018/6/24
 * 时间：At 22:22
 */
public enum Z_TYPE
{
    TEXT(TextBuilder.class),
    STAR_LOADING(StarBuilder.class);

    private final Class<?> mBuilderClass;

    Z_TYPE(Class<?> builderClass)
    {
        this.mBuilderClass = builderClass;
    }

    <T extends ZLoadingBuilder> T newInstance()
    {
        try
        {
            return (T) mBuilderClass.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
