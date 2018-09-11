var _value = Vue.component('value', {
    template : "#value-template",
    props: {
        values:[],
        labelField: 'label',
        fieldName: 'cb',
        valueField: 'id',
        numcols: 2,
        type:'checkbox',
        choices: []
    },
    methods: {
        subarray: function(start) {
        var num = Math.ceil(this.choices.length / this.numcols);
        var init = start * num;
        var ret =  this.choices.slice(init , init + num);
        return ret;
        },
        collist: function() {
            return [...Array(this.numcols).keys()];
        }
    }
});