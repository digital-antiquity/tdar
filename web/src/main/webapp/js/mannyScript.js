
$('.tdarNavItem').click(function()
{
    if ($(this).find('.nav-chevron').attr('transform'))
        $(this).find('.nav-chevron').removeAttr('transform');
    else
        $(this).find('.nav-chevron').attr('transform', 'rotate(180)');
});