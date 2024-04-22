import { Store } from './store'
import mixins from './mixins/helpers'

type Mixins = typeof mixins.methods;

declare module 'vue' {
  interface ComponentCustomProperties extends Mixins {
    $store: Store;
  }
}
