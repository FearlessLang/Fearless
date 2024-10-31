use std::mem::MaybeUninit;

#[derive(Debug)]
pub struct BoundedStack<const SIZE: usize, T> {
	inner: Box<[MaybeUninit<T>; SIZE]>,
	next: usize,
}
impl<const SIZE: usize, T> Default for BoundedStack<SIZE, T> {
    fn default() -> Self {
        Self::new()
    }
}

impl<const SIZE: usize, T> BoundedStack<SIZE, T> {
	pub fn new() -> Self {
		Self {
			inner: Box::new([const { MaybeUninit::uninit() }; SIZE]),
			next: 0,
		}
	}

	pub fn push(&mut self, value: T) {
		assert!(self.next < SIZE);
		self.inner[self.next].write(value);
		self.next += 1;
	}

	pub fn peek(&mut self) -> Option<&T> {
		if self.next == 0 {
			None
		} else {
			let elem = &mut self.inner[self.next - 1];
			// Safety: All elements before the `next` index are initialized.
			Some(unsafe { elem.assume_init_ref() })
		}
	}

	pub fn pop(&mut self) -> Option<T> {
		if self.next == 0 {
			None
		} else {
			// Safety: All elements before the `next` index are initialized and no elements after it are initialized.
			self.next -= 1;
			let elem = std::mem::replace(&mut self.inner[self.next], MaybeUninit::uninit());
			Some(unsafe { elem.assume_init() })
		}
	}

	pub fn get(&self, index: usize) -> Option<&T> {
		if index < self.next {
			// Safety: All elements before the `next` index are initialized.
			Some(unsafe { self.inner[index].assume_init_ref() })
		} else {
			None
		}
	}
	
	pub fn is_empty(&self) -> bool {
		self.next == 0
	}
}
impl<'a, const SIZE: usize, T> IntoIterator for &'a BoundedStack<SIZE, T> {
	type Item = &'a T;
	type IntoIter = BoundedStackIterator<'a, SIZE, T>;

	fn into_iter(self) -> Self::IntoIter {
		let cursor = if self.is_empty() { 0 } else { self.next - 1 };
		BoundedStackIterator {
			stack: self,
			next: cursor,
		}
	}
}
impl<const SIZE: usize, T: Clone> Clone for BoundedStack<SIZE, T> {
	fn clone(&self) -> Self {
		let mut clone = Self::new();
		for elem in self.into_iter() {
			clone.push(elem.clone());
		}
		clone
	}
}

pub struct BoundedStackIterator<'a, const SIZE: usize, T> {
	stack: &'a BoundedStack<SIZE, T>,
	next: usize,
}
impl<'a, const SIZE: usize, T> Iterator for BoundedStackIterator<'a, SIZE, T> {
	type Item = &'a T;

	fn next(&mut self) -> Option<Self::Item> {
		let result = self.stack.get(self.next);
		self.next -= 1;
		result
	}
}
