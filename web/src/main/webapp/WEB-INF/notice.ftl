<div class="container">
<div class="row">
<div class='info'>
    <div class="alert alert-<#if production>secondary<#else>warning</#if> pt-4 mt-4">
<#if !production>

        <p>
            <strong>Note:</strong>
            You are using tDAR's <strong>staging development server</strong>. This server is only
            for testing purposes and data stored here will <strong>NOT</strong> be preserved. If
            you'd like to use tDAR proper, please visit <a href='http://core.tdar.org'>the official tDAR site</a>.
        </p>

        <p>
            For more information you can contact us at <a
                href="${commentUrlEscaped}?subject=tDAR%20comments">&#99;&#111;&#109;&#109;&#101;&#110;&#116;&#115;&#64;&#116;&#100;&#97;&#114;&#46;&#111;&#114;&#103;</a>.
        </p>


<#else>
    You are using tDAR. Content will be
    subject to review, including for appropriateness to Digital Antiquity's objectives
    and conformance with tDAR's contributor agreement and its basic metadata
    requirements.
    <br><br>
    We appreciate and encourage you to send comments, suggestions, and bug reports to
    <a
            href="${commentUrlEscaped}?subject=tDAR%20comments">&#99;&#111;&#109;&#109;&#101;&#110;&#116;&#115;&#64;&#116;&#100;&#97;&#114;&#46;&#111;&#114;&#103;</a>
</#if>
    </div>
</div>
</div>
</div>
