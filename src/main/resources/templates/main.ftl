<#include "temp/mainTemplate.ftl">
<@main_template title="Поиск"/>

<#macro body>

<div class="allContent">
    <h2>Поиск</h2>

    <form action="/search" method="POST">
        <input type="text" name="text" value="${(text)!}"/>
        <button type="submit">Поиск</button>
    </form>
    <#if results?has_content>
    <br>
    <p>Найдено</p>
        <#list results as result>
            <p><a href="${(result)!}">${(result)!}</a></p>
        </#list>
    <#else><p>${(answer)!}</p>
    </#if>

</div>
</#macro>