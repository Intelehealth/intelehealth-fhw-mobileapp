import ReactTestRenderer, {ReactTestInstance} from 'react-test-renderer';
import {Text} from 'react-native';

/** Concatenated text of a node and all its descendants. */
export function textOf(node: ReactTestInstance | string): string {
  if (typeof node === 'string') {
    return node;
  }
  return node.children.map(textOf).join('');
}

/** Text of every <Text> element (outer ones include their nested children). */
export function texts(root: ReactTestInstance): string[] {
  return root.findAllByType(Text).map(textOf);
}

export function hasText(root: ReactTestInstance, expected: string): boolean {
  return texts(root).some(t => t === expected);
}

export function hasTextContaining(root: ReactTestInstance, part: string): boolean {
  return texts(root).some(t => t.includes(part));
}

/** Pressable element (anything with an onPress handler) whose text matches. */
export function pressableWithText(root: ReactTestInstance, label: string): ReactTestInstance {
  const matches = root.findAll(
    n => typeof n.props.onPress === 'function' && textOf(n).includes(label),
  );
  if (matches.length === 0) {
    throw new Error(`No pressable with text "${label}". Texts: ${texts(root).join(' | ')}`);
  }
  // Innermost match: the closest pressable around that label.
  return matches[matches.length - 1];
}

export async function render(element: React.ReactElement) {
  let renderer!: ReactTestRenderer.ReactTestRenderer;
  await ReactTestRenderer.act(async () => {
    renderer = ReactTestRenderer.create(element);
  });
  mounted.push(renderer);
  return renderer;
}

const mounted: ReactTestRenderer.ReactTestRenderer[] = [];

// Unmount after every test so list/ticker timers don't fire into a finished test.
afterEach(() => {
  ReactTestRenderer.act(() => {
    mounted.splice(0).forEach(r => r.unmount());
  });
});

export async function act(fn: () => void) {
  await ReactTestRenderer.act(async () => {
    fn();
  });
}

/** ISO instant `seconds` from the (possibly faked) current time. */
export function inSeconds(seconds: number): string {
  return new Date(Date.now() + seconds * 1000).toISOString();
}
