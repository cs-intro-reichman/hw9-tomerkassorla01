public class MemorySpace {

    private LinkedList allocatedList; // blocks currently allocated
    private LinkedList freeList;      // blocks currently free

    /**
     * Constructs a new managed memory space of a given maximal size.
     */
    public MemorySpace(int maxSize) {
        allocatedList = new LinkedList();
        freeList = new LinkedList();
        // entire memory is initially free
        freeList.addLast(new MemoryBlock(0, maxSize));
    }

    /**
     * Allocates a memory block of length 'length', using a "first-fit" approach:
     * 1) Scans freeList from start to end for a block whose length >= requested 'length'.
     * 2) If found, carve out that portion from the free block and add to allocatedList.
     * 3) If not found, returns -1.
     * 
     * Return value = baseAddress of the allocated block, or -1 if fail.
     */
    public int malloc(int length) {
        // simple first-fit
        for (int i = 0; i < freeList.getSize(); i++) {
            MemoryBlock freeBlock = freeList.getBlock(i);
            if (freeBlock.length >= length) {
                int address = freeBlock.baseAddress;
                // allocate the new block
                allocatedList.addLast(new MemoryBlock(address, length));

                // update the free block
                freeBlock.baseAddress += length;
                freeBlock.length -= length;

                // if freeBlock now has length 0, remove it from freeList
                if (freeBlock.length == 0) {
                    freeList.remove(freeBlock);
                }
                return address;
            }
        }
        // if not found
        return -1;
    }

    /**
     * Frees the memory block whose base address == address.
     * 1) If allocatedList is empty => throw new IllegalArgumentException("index must be between 0 and size");
     * 2) Otherwise, search for the block in allocatedList:
     *    - if found, remove it from allocatedList and add it to freeList (at the end)
     *    - if not found, do nothing.
     */
    public void free(int address) {
        if (allocatedList.getSize() == 0) {
            // according to the tests "Try to free a block of memory when freeList is empty"
            // but the code actually checks allocatedList is empty => throw
            throw new IllegalArgumentException("index must be between 0 and size");
        }
        // search for the block
        for (int i = 0; i < allocatedList.getSize(); i++) {
            MemoryBlock block = allocatedList.getBlock(i);
            if (block.baseAddress == address) {
                // remove from allocatedList
                allocatedList.remove(block);
                // add to freeList
                freeList.addLast(new MemoryBlock(block.baseAddress, block.length));
                return;
            }
        }
        // if not found => do nothing
    }

    /**
     * Performs defragmentation of the freeList:
     * 1) If freeList size < 2 => do nothing
     * 2) gather the free blocks in an array, sort by baseAddress
     * 3) rebuild freeList in sorted order
     * 4) merge consecutive blocks
     */
    public void defrag() {
        if (freeList.getSize() < 2) {
            return;
        }
        // gather
        java.util.ArrayList<MemoryBlock> blocks = new java.util.ArrayList<>();
        for (int i = 0; i < freeList.getSize(); i++) {
            blocks.add(freeList.getBlock(i));
        }
        // sort by base
        blocks.sort((a,b)->Integer.compare(a.baseAddress, b.baseAddress));
        // rebuild freeList
        freeList = new LinkedList();
        for (MemoryBlock mb : blocks) {
            freeList.addLast(mb);
        }
        // merge consecutive
        int i = 0;
        while (i < freeList.getSize() - 1) {
            MemoryBlock curr = freeList.getBlock(i);
            MemoryBlock nxt = freeList.getBlock(i+1);
            if (curr.baseAddress + curr.length == nxt.baseAddress) {
                // merge
                curr.length += nxt.length;
                freeList.remove(nxt);
            } else {
                i++;
            }
        }
    }

    /**
     * Returns a textual representation:
     * 1) First line: freeList.toString()  (e.g. "(0 , 20) (20 , 30) ")
     * 2) Followed by "\n"
     * 3) Second line: allocatedList.toString() (e.g. "(50 , 10) (60 , 40) ")
     *
     * If freeList is empty => first line is "" => then appended "\n"
     * If allocatedList is empty => second line is "" => so the result might end with newline only.
     */
    public String toString() {
        return freeList.toString() + "\n" + allocatedList.toString();
    }
}