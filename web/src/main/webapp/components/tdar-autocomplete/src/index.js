import Autocomplete from './components/Autocomplete';

export default {
    install(Vue) {
        Vue.component('autocomplete', Autocomplete);
    },
};

export { Autocomplete };